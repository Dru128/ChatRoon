package com.example.chatroon

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity()
{
    var countID = 0
    val database = FirebaseDatabase.getInstance()
    val myRef = database.reference

    /**
    "Сообщение"
    Класс для обмена данными с базой данных
    про "data" можно почитать здесь: https://kotlinlang.ru/docs/reference/data-classes.html
    Обрати внимание, что он объявляется с круглыми скобками
     */
    public data class Message
        (
        var Text:String = "",
        var Author:String = ""
        )

    /**
    А это уже список сообщений. Он будет заполняться на из объектов, которые будут загружаться\
    из базы
     */
    val messageList: MutableCollection<Message> = mutableListOf()

    //------------
    override fun onCreate(savedInstanceState: Bundle?)
    {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /**
         * Обработчик события - "листенер"
         */
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //Очищаем список сообщений
                messageList.clear()
                // Тут происходит МАГИЯ!
                // Из потока данных, возвращаемых из БД, который есть текстовое представление списка объектов
                // восстанавливаются объекты типа Message и записываются в список messageList
                dataSnapshot.children.mapNotNullTo(messageList) { it.getValue<Message>(Message::class.java) }
                // Полезная работа - в данном случае, посторочно пишем сообщения в текстбокс
                myLayout.removeAllViews()
                messageList.forEach {
                    val btn = Button(applicationContext)
                    btn.text = "${it.Author}: ${it.Text}\n"
                    btn.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
                    btn.id = countID
                    btn.setOnClickListener { v -> myLayout!!.removeView(v) }
                    myLayout!!.addView(btn)
                    countID++
                }
z

            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("loadPost:onCancelled ${databaseError.toException()}")
            }
        }

        /**
         * Подписка на событие изменения списка - добавление листенера
         * Почитать про листенеры и поэкспериментировать с разными листенерами
         * они отличаются по событиям: изменение свойств существующего объекта, появление новых
         * дочерних объектов и т.д.
         */
        //myRef.child("messages").addListenerForSingleValueEvent(menuListener)
        myRef.child("messages").addValueEventListener(menuListener)
    }

    fun click(v: View)
    {
        // Сохранение в БД
        // Создали объект, который будем писать в базу
        val msg : Message = Message(input_text.text.toString(), "Rabbit")

        // Записали пустой дочерний узел и получили ключ
        val key = myRef.child("messages").push().key
        // Если узел создался, то находим его по ключу и присваиваем ему нащ объект
        if (key != null)
        {
            myRef.child("messages").child(key).setValue(msg)
        }


    }
}
