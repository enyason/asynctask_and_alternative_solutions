package com.enyason.asynctask_and_alternative_solutions

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.enyason.asynctask_and_alternative_solutions.databinding.ActivityMainBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.math.BigInteger
import kotlin.concurrent.thread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {


    private val input = 50000
    private val disposableContainer = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        DoWorkAsync(binding).execute(input)
//        coDoWork(binding)
        doWorkRx(binding)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposableContainer.clear()
    }


    private fun doWorkRx(binding: ActivityMainBinding) {
        val observer = object : Observer<BigInteger> {
            override fun onSubscribe(disposable: Disposable) {
                disposableContainer.add(disposable)
            }

            override fun onNext(result: BigInteger) {
                val text = "Factorial of $result = $result"
                binding.displayText.text = text
            }

            override fun onError(error: Throwable) {
                binding.displayText.text = error.message
            }

            override fun onComplete() {
                println("operation completed")
            }
        }


        val observable = Observable.create<BigInteger> {
            it.onNext(factorial(input))
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())


        observable.subscribe(observer)
    }


    private fun coDoWork(binding: ActivityMainBinding) {
        lifecycleScope.launch(Dispatchers.IO) {
            println("running task on ${Thread.currentThread().name}")
            val result = factorial(input)
            val text = "Factorial of $result = $result"
            withContext(Dispatchers.Main) {
                println("Accessing UI on ${Thread.currentThread().name}")
                binding.displayText.text = text
            }
        }
        println("running end on ${Thread.currentThread().name}")

    }

    class DoWorkAsync(private val binding: ActivityMainBinding) :
        AsyncTask<Int, Unit, AsyncResult>() {

        override fun onPreExecute() {
            super.onPreExecute()
            println("running onPreExecute on ${Thread.currentThread().name}")
            binding.displayText.text = "heavy calculation ongoing..."
        }

        override fun doInBackground(vararg params: Int?): AsyncResult {
            println("running doInBackground on ${Thread.currentThread().name}")
            val param = params.first() ?: return AsyncResult.Error
            val factorial = factorial(param)
            return AsyncResult.Success(param, factorial)
        }

        override fun onPostExecute(result: AsyncResult?) {
            println("running onPostExecute on ${Thread.currentThread().name}")
            super.onPostExecute(result)
            when (result) {
                is AsyncResult.Success -> "Factorial of ${result.input} = ${result.value}"
                AsyncResult.Error, null -> "An error occurred while computing the value"
            }.also { binding.displayText.text = it }
        }
    }

    sealed class AsyncResult {
        data class Success(val input: Int, val value: BigInteger) : AsyncResult()
        object Error : AsyncResult()
    }
}

private fun factorial(number: Int): BigInteger {
    var factorial: BigInteger = BigInteger.ONE

    for (curNum in 1..number) {
        factorial = factorial.multiply(BigInteger.valueOf(curNum.toLong()))
    }

    return factorial
}
