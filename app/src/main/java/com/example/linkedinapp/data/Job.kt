package com.example.linkedinapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jobs")
data class Job(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String, // Название вакансии
    val salaryFrom: Int? = null, // Зарплата от
    val salaryTo: Int? = null, // Зарплата до
    val salaryCurrency: String = "руб.", // Валюта зарплаты
    val experience: String, // Опыт работы
    val resume: String, // Описание
    val city: String, // Город
    val aboutUs: String, // О нас
    val requiredQualities: String, // Нужные качества
    val weOffer: String, // Мы предлагаем
    val keySkills: String, // Ключевые навыки
    val employerId: Long, // ID работодателя (связь с User)
    val createdAt: Long = System.currentTimeMillis() // Время создания
) {
    // Для обратной совместимости - формируем строку зарплаты
    fun getSalaryString(): String {
        return when {
            salaryFrom != null && salaryTo != null -> "$salaryFrom - $salaryTo $salaryCurrency"
            salaryFrom != null -> "от $salaryFrom $salaryCurrency"
            salaryTo != null -> "до $salaryTo $salaryCurrency"
            else -> ""
        }
    }
    
    // Получаем минимальную зарплату для фильтрации
    fun getMinSalary(): Int? = salaryFrom
}

