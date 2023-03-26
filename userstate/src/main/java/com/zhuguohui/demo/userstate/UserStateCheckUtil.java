package com.zhuguohui.demo.userstate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mdit.library.dx.stock.ProxyBuilder;
import com.mdit.library.proxy.Enhancer;
import com.mdit.library.proxy.MethodInterceptor;
import com.mdit.library.proxy.MethodProxy;
import com.zhuguohui.demo.userstate.function.CallBack;
import com.zhuguohui.demo.userstate.function.Function1;
import com.zhuguohui.demo.userstate.manager.UserStateManager;
import com.zhuguohui.demo.userstate.policy.UserStateCheckPolicy;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/3/13
 * Time: 15:50
 * Desc:用于检查用户状态的工具类
 * 提供了两种方法
 * {@link #justCheck(Context,IUserState...)} 只检测
 * {@link #checkAndGoto(Context,IUserState...)} 检测加跳转
 *
 * </pre>
 */
public class UserStateCheckUtil {
    private static final Object EMPTY_OBJ = new Object();

    public static Observable<Object> justCheck(Context context,IUserState... userStates) {
        return Observable.just(EMPTY_OBJ)
                .compose(new UserStateTransform<>(context,true, userStates));
    }

    public static Observable<Object> checkAndGoto(Context context,IUserState... userStates) {
        return Observable.just(EMPTY_OBJ)
                .compose(new UserStateTransform<>(context,false, userStates));
    }

    public static <T> T getProxy(CompositeDisposable compositeDisposable, Context context, T delegate, CallBack<Throwable> errorFunction) {
        try {
         return (T)   ProxyBuilder.forClass((Class<T>)delegate.getClass())
                    .setMethodOverrideFilter(method -> method.getAnnotation(CheckUserState.class)!=null)
                    .dexCache(context.getDir("dx", Context.MODE_PRIVATE))
                    .handler(new InvocationHandler() {



                        private Method getMethodByName(Class clazz,String name){
                            Method[] methods = clazz.getMethods();
                            for(int i=0;i<methods.length;i++){
                                if(methods[i].getName().equals(name)){
                                    return methods[i];
                                }
                            }
                            return null;
                        }
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                             method = getMethodByName(delegate.getClass(),method.getName());
                            CheckUserState userState = method.getAnnotation(CheckUserState.class);
                            if (userState != null) {
                                UserStateCheckPolicy policy = userState.policy();
                                Observable<Object> observable = policy == UserStateCheckPolicy.justCheck ?
                                        justCheck(context,UserStateManager.getInstance().getUserStateByFlags(userState.states()))
                                        : checkAndGoto(context,UserStateManager.getInstance().getUserStateByFlags(userState.states()));
                                Method finalMethod = method;
                                Disposable disposable = observable
                                        .subscribe(obj -> {
                                            finalMethod.invoke(delegate,args);
                                        }, e -> {
                                            if(errorFunction!=null){
                                                errorFunction.call(e);
                                            }
                                        });
                                compositeDisposable.add(disposable);

                                return null;

                            }
                            return    ProxyBuilder.callSuper(proxy, method, args);
                        }
                    }).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String appNameSpace = "http://schemas.android.com/apk/res-auto";
    /**
     * <pre>
     * 设置 布局填充器的Factory
     * 必须在activity的onCreate()之前调用
     * <code>
     *         UserStateCheckUtil.setLayoutInflaterFactory(this);
     *         super.onCreate(savedInstanceState);
     * </code>
     * </pre>
     * @param activity
     */
    public static LayoutInflater.Factory2 getLayoutInflaterFactory(AppCompatActivity activity, CompositeDisposable compositeDisposable,  CallBack<Throwable> errorFunction) {

        LayoutInflater.Factory2 factory2 = new LayoutInflater.Factory2() {


            @Nullable
            @Override
            public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
                View view = activity.getDelegate().createView(parent, name, context, attrs);
                int userStateFlags = attrs.getAttributeIntValue(appNameSpace, "checkUserState", 0);
                int checkUserPolicy = attrs.getAttributeIntValue(appNameSpace, "checkUserPolicy", 0);
                UserStateCheckPolicy policy = checkUserPolicy == 0 ? UserStateCheckPolicy.autoGo : UserStateCheckPolicy.justCheck;
                if (userStateFlags != 0) {
                    Class viewClass=null;
                    try{
                        if(view==null){
                            if(!name.contains(".")){
                                name="android.widget."+name;
                            }

                            viewClass=Class.forName(name);
                        }else{
                            viewClass=view.getClass();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return UserStateCheckUtil.getProxyForView(activity,
                            compositeDisposable,
                            userStateFlags,
                            policy, viewClass, attrs,errorFunction);
                }
                return view;
            }

            @Nullable
            @Override
            public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
                return onCreateView(null, name, context, attrs);
            }
        };
       return factory2;
    }
    private static View getProxyForView( Context context,
                                         CompositeDisposable compositeDisposable,
                                         int userStateFlags,
                                         UserStateCheckPolicy policy,
                                         Class viewClass,
                                         AttributeSet attrs,
                                         CallBack<Throwable> errorFunction) {

        try {
             return (View) ProxyBuilder.forClass(viewClass)
                    .constructorArgTypes(Context.class, AttributeSet.class)
                    .constructorArgValues(context, attrs)
                    .dexCache(context.getDir("dx", Context.MODE_PRIVATE))
                     //只重写setOnClickListener
                     // 因为android中的View中的一些方式被 @UnsupportedAppUsage注解修饰
                     //客户端无法通过反射来获取，无法重写
                     .setMethodOverrideFilter(method -> method.getName().equals("setOnClickListener"))
                    .handler(new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] objects) throws Throwable {
                            if(method.getName().equals("setOnClickListener")){
                                if(objects.length>0&& objects[0] instanceof View.OnClickListener){
                                    View.OnClickListener onClickListener= (View.OnClickListener) objects[0];
                                    objects=new Object[]{new CheckUserStateOnClickListener(context,compositeDisposable,onClickListener,userStateFlags,policy,errorFunction)};

                                }
                            }

                         return    ProxyBuilder.callSuper(proxy, method, objects);
                        }
                    }).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class CheckUserStateOnClickListener implements View.OnClickListener{
        private  UserStateCheckPolicy policy;
        View.OnClickListener clickListener;
        int userStateFlags;
        private IUserState[] userStates;
        CallBack<Throwable> errorFunction;
        Context context;
        CompositeDisposable compositeDisposable;
        public CheckUserStateOnClickListener(Context context,
                                             CompositeDisposable compositeDisposable,
                                             View.OnClickListener clickListener,
                                             int userStateFlags,
                                             UserStateCheckPolicy policy,
                                             CallBack<Throwable> errorFunction) {
            this.clickListener = clickListener;
            this.userStateFlags = userStateFlags;
            this.errorFunction=errorFunction;
            this.context=context;
            this.compositeDisposable=compositeDisposable;
            userStates = UserStateManager.getInstance().getUserStateByFlags(userStateFlags);
            this.policy=policy;
        }

        public CheckUserStateOnClickListener(View.OnClickListener clickListener) {
            this.clickListener = clickListener;
        }

        @Override
        public void onClick(View v) {
            Observable<Object> observable=policy==UserStateCheckPolicy.autoGo?checkAndGoto(context,userStates):justCheck(context,userStates);
            Disposable disposable = observable
                    .subscribe(obj -> {
                        clickListener.onClick(v);
                    }, e -> {
                        if(errorFunction!=null) {
                            errorFunction.call(e);
                        }
                    });
            compositeDisposable.add(disposable);
        }
    }
}
