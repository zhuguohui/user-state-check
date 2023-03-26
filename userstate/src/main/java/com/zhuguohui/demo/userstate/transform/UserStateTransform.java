package com.zhuguohui.demo.userstate.transform;

import android.content.Context;
import android.os.Looper;

import com.zhuguohui.demo.userstate.IUserState;
import com.zhuguohui.demo.userstate.UserStateCallBack;
import com.zhuguohui.demo.userstate.UserStateCheckException;
import com.zhuguohui.demo.userstate.manager.UserStateManager;
import com.zhuguohui.demo.userstate.policy.UserStateCheckPolicy;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/2/28
 * Time: 15:30
 * Desc:
 * </pre>
 */
public class UserStateTransform<T> implements ObservableTransformer<T, T> {
    private  Context context;
    private List<IUserState> userStateList;
    private UserStateCheckPolicy policy;

    public UserStateTransform(Context context,int userStateFlags) {
        this(context,false, userStateFlags);
    }

    public UserStateTransform(Context context,boolean justCheck, int userStateFlags) {
        this(context,justCheck ? UserStateCheckPolicy.justCheck : UserStateCheckPolicy.autoGo, userStateFlags);

    }

    public UserStateTransform(Context context,UserStateCheckPolicy policy,int userStateFlags) {

        this.context=context;
        IUserState[] states = UserStateManager.getInstance().getUserStateByFlags(userStateFlags);
        userStateList=Arrays.asList(states);
        this.policy = policy == null ? UserStateCheckPolicy.autoGo : policy;

    }

    private static Object object = new Object();
    Scheduler scheduler;

    @Override
    public ObservableSource<T> apply(Observable<T> upstream) {
        //先验证用户状态
        return Observable.just(object)
                .doOnNext(obj -> scheduler = getCallSchedulers())
                .flatMap(obj->{
                    Observable<Object> next=Observable.just(obj);
                    for(IUserState userState:userStateList){
                        next=next.flatMap(o->matchUserState(o,userStateList,userState));
                    }
                    return next;
                })
                .flatMap(obj -> upstream) //全部验证通过才订阅上游
                ;

    }

    /**
     * 获取上游的Scheduler,这样在回调的时候切换回原先的Scheduler
     *
     * @return
     */
    private Scheduler getCallSchedulers() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return AndroidSchedulers.mainThread();
        } else {
            return Schedulers.io();
        }
    }


    private Observable<Object> matchUserState(Object t, List<IUserState> userStateList, IUserState userState) {
        boolean needMatch = userStateList.contains(userState);
        if (needMatch && !UserStateManager.getInstance().isMatchUserState(userState)) {
            if (policy == UserStateCheckPolicy.autoGo) {
                return Observable.create((ObservableOnSubscribe<Object>) emitter -> {
                    UserStateManager.getInstance().doMatchUserState(context,userState, new UserStateCallBack() {
                        @Override
                        public void onMath() {
                            scheduler.scheduleDirect(() -> {
                                //登录成功
                                emitter.onNext(t);
                                emitter.onComplete();
                            });

                        }

                        @Override
                        public void onCancel() {
                            scheduler.scheduleDirect(() -> {
                                emitter.onError(new UserStateCheckException(userState));
                            });

                        }
                    });
                });
            } else {
                return Observable
                        .error(new UserStateCheckException(userState))
                        .observeOn(scheduler);
            }
        } else {
            return Observable
                    .just(t)
                    .subscribeOn(scheduler);
        }
    }

}
