package com.zhuguohui.demo.userstate;



import android.content.Context;

import com.zhuguohui.demo.userstate.function.Function1;
import com.zhuguohui.demo.userstate.manager.UserStateManager;
import com.zhuguohui.demo.userstate.policy.UserStateCheckPolicy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/3/1
 * Time: 11:34
 * Desc:用于实现用户状态的检查
 * 需要配合{@link CheckUserState}注解来使用
 * </pre>
 */
public class UserStateCheckIntercept implements Interceptor {
    private final Object lock = new Object();
    private UserInfoUpdate userInfoUpdate;
    private Function1<Void, Context> getContextFunction;
    public UserStateCheckIntercept(UserInfoUpdate userInfoUpdate, Function1<Void, Context> getContextFunction) {
        this.userInfoUpdate = userInfoUpdate;
        this.getContextFunction=getContextFunction;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Invocation invocation = request.tag(Invocation.class);
        CheckUserState userState = null;
        if(invocation != null) {
            userState= invocation.method().getAnnotation(CheckUserState.class);
        }
        if (userState != null) {
            //checkUserState
            UserStateCheckPolicy policy = request.tag(UserStateCheckPolicy.class);
            IUserState[] value = UserStateManager.getInstance().getUserStateByFlags(userState.states());
            synchronized (lock) {
                AtomicReference<RuntimeException> checkError = new AtomicReference<>();
                Disposable disposable = Observable.just(new Object())
                        .compose(new UserStateTransform<>(getContextFunction.call(null),policy, value))
                        .subscribe(obj -> {
                            synchronized (lock) {
                                lock.notify();
                            }
                        }, e -> {
                            checkError.set((RuntimeException) e);
                            synchronized (lock) {
                                lock.notify();
                            }
                        });
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (checkError.get() != null) {
                    throw checkError.get();
                }else{
                    //更新request中的用户信息
                    request=userInfoUpdate.updateUserInfo(request);
                }
            }
        }
        return chain.proceed(request);
    }
}
