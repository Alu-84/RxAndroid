package it.alfionte.rxandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Callable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    public static final String URL = "http://www.google.com";
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //    ------------- observers

        Observer<String> ob = new Observer<String>() {
            @Override
            public void onCompleted() {
                Log.d("WOO", "WOO complete!");
            }

            @Override
            public void onError(Throwable e) {
                Log.d("WOO", "WOO WTF?: " + e.getMessage());
            }

            @Override
            public void onNext(String s) {
                Log.d("WOO", "WOO: " + s);
            }
        };

        //    ------------- observables

        subscription = Observable.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return ping(URL);
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer code) {
                        return code == 200;
                    }
                })
                .map(new Func1<Integer, String>() {
                         @Override
                         public String call(Integer code) {
                             String s = "The ping on" + URL + " is ";
                             switch (code) {
                                 case 200:
                                     s += "good";
                                     break;
                                 default:
                                     s += "bad";
                             }
                             return s;
                         }
                     }

                )
                .subscribe(ob);
    }

    private Integer ping(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.code();
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }
}
