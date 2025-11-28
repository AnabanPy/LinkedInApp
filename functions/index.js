const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

/**
 * Cloud Function, которая слушает коллекцию pending_notifications
 * и отправляет push-уведомления через FCM
 */
exports.sendMessageNotification = functions.firestore
    .document('pending_notifications/{notificationId}')
    .onCreate(async (snap, context) => {
        const notificationData = snap.data();
        
        // Проверяем, не было ли уже отправлено
        if (notificationData.sent) {
            return null;
        }
        
        const receiverId = notificationData.receiverId;
        const senderName = notificationData.senderName;
        const messageText = notificationData.messageText;
        const senderId = notificationData.senderId;
        
        try {
            // Получаем все токены получателя
            const tokensSnapshot = await admin.firestore()
                .collection('user_tokens')
                .doc(receiverId)
                .collection('tokens')
                .get();
            
            if (tokensSnapshot.empty) {
                // У получателя нет токенов
                await snap.ref.update({ sent: true, error: 'No tokens found' });
                return null;
            }
            
            // Собираем все токены
            const tokens = tokensSnapshot.docs.map(doc => doc.id);
            
            // Создаем payload для уведомления
            const message = {
                notification: {
                    title: senderName,
                    body: messageText
                },
                data: {
                    senderId: senderId,
                    senderName: senderName,
                    messageText: messageText,
                    type: 'message'
                },
                tokens: tokens
            };
            
            // Отправляем уведомление на все устройства получателя
            const response = await admin.messaging().sendEachForMulticast(message);
            
            // Удаляем успешно отправленные токены (если они недействительны)
            const failedTokens = [];
            response.responses.forEach((resp, idx) => {
                if (!resp.success) {
                    failedTokens.push(tokens[idx]);
                }
            });
            
            // Удаляем недействительные токены
            if (failedTokens.length > 0) {
                const batch = admin.firestore().batch();
                failedTokens.forEach(token => {
                    const tokenRef = admin.firestore()
                        .collection('user_tokens')
                        .doc(receiverId)
                        .collection('tokens')
                        .doc(token);
                    batch.delete(tokenRef);
                });
                await batch.commit();
            }
            
            // Помечаем уведомление как отправленное
            await snap.ref.update({ 
                sent: true, 
                sentAt: admin.firestore.FieldValue.serverTimestamp(),
                successCount: response.successCount,
                failureCount: response.failureCount
            });
            
            return null;
        } catch (error) {
            console.error('Error sending notification:', error);
            await snap.ref.update({ 
                sent: true, 
                error: error.message 
            });
            return null;
        }
    });

