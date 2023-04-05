# user-state-check
基于AOP实现用户状态检测的框架



# 功能

- 通过dexmaker 实现动态代理，通过设置ViewFactory2，动态生成view的子类。配合xml中定义属性。可以无感的拦截任意view的点击事件
- 通过dexMaker 实现AOP，可以生成任意类的子类。便于和viewDataBing联合使用。
- 可以和RxJava联合使用。
- 可以自定义多个用户状态(最多32个，用的int保存的，可以自行扩展成long类型)
- 可以自动跳转相关页面

# 示例

所有的示例都在demo的**MainActivity**中

## 1.配合viewDataBiding使用

### 布局文件如下

```xml
    <RelativeLayout
                    android:layout_width="match_parent"
                    android:layout_height="100dp"
                    android:background="#eee">

                    <Button
                        android:id="@+id/btn_collection"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_centerInParent="true"
                        android:onClick="@{activity::doCollection}"
                        android:text="收藏(需要登陆,绑定手机号,实名认证)"
                        tools:ignore="HardcodedText" />
                </RelativeLayout>
```



### 在需要拦截的方法上使用注解

设置需要检测的状态为: 登录，绑定手机号，实名认证。

```java
    @CheckUserState(states = Login|BindPhoneNumber|BindRealName,policy = 	UserStateCheckPolicy.autoGo)
    public void doCollection(View view){
        GZToast.success().show("收藏成功");
    }
```

### 动态生成Activity的子类

```java
 private void aopActivity() {
        binding.setActivity( UserStateManager.getProxy(compositeDisposable,this,this,e->{
            GZToast.error().show(e.getMessage());
        }));
    }
```



### 效果

在执行收藏的之前，会检测用户状态，如果用户状态不满足，会自动跳转到相关页面。最后全面满足以后会自动执行收藏操作。

<img src=".\demo-img\aop其他类.gif" style="zoom:50%;" />

## 2.拦截View的点击事件

### 1.设置ViewFactory2

注意需要在onCreate()方法之前注入。避免AppCompatActivity先注入

```java
   @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //注入factory2
        LayoutInflaterCompat.setFactory2(LayoutInflater.from(this), UserStateManager.getLayoutInflaterFactory(this,compositeDisposable, e->{
            GZToast.error().show(e.getMessage());
        }));
        super.onCreate(savedInstanceState);
        currentActivity=this;
    }
```

### 设置view要检测的状态

通过 **app:checkUserPolicy**设置要检测的状态

通过**app:checkUserPolicy**设置状态不满足情况下的执行策略

包含两种:

|  策略值   |            执行的动作            |
| :-------: | :------------------------------: |
|  autoGo   |        自动跳转到相关界面        |
| justCheck | 只检查状态，如果不满足会抛出异常 |



```xml
  <LinearLayout
                        android:id="@+id/layout_collection"
                        android:layout_width="100dp"
                        android:layout_height="100dp"
                        android:layout_centerVertical="true"
                        android:layout_marginStart="10dp"
                        android:background="#25DA6E"
                        android:orientation="vertical"
                        app:checkUserPolicy="autoGo"
                        app:checkUserState="login">

                        <TextView
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:layout_gravity="center"
                            android:gravity="center"
                            android:text="拦截LinearLayout 需要登录以后才能执行收藏，没有登录自动登录"
                            android:textColor="@color/white" />
                    </LinearLayout>
```



### 使用

正常设置点击事件即可，通过框架实现拦截工作对业务代码无感。

```java
 private void aopView() {
        binding.layoutCollection.setOnClickListener(v->{
            GZToast.success().show("收藏成功");
        });
        binding.tvCollection.setOnClickListener(v -> {
            GZToast.success().show("收藏成功");
        });
    }
```

### 效果

<img src=".\demo-img\拦截view.gif" style="zoom:50%;" />

## 3.和RXJava配合使用

重点是下面这句

 **compose(new UserStateTransform<>(this, Login|BindRealName))**

```java
  private void aopApi() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://tenapi.cn/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        BaiduApi baiduApi = retrofit.create(BaiduApi.class);

        BaiduApi finalBaiduApi = baiduApi;
        binding.btnGetBaiduHot.setOnClickListener(v->{
            Disposable disposable = finalBaiduApi.getHotList()
                    //检测用户状态
                    .compose(new UserStateTransform<>(this, Login|BindRealName))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(BaiduHotListResult::getData)
                    .subscribe(list -> {
                        StringBuffer buffer=new StringBuffer();
                        for(HotItemBean itemBean:list){
                            buffer.append(itemBean.getName()).append("\n");
                        }
                        binding.tvBaiduContent.setText(buffer);
                    }, e -> {
                        e.printStackTrace();
                        GZToast.error().show(e.getMessage());
                    });
            compositeDisposable.add(disposable);
        });
    }
```



### 效果

<img src=".\demo-img\rxjava.gif" style="zoom:50%;" />

## 用户不同意

会抛出**UserStateCheckException**通过其 **getState()** 方法可以获取匹配不成功的用户状态

在动态代理相关类的时候都可以传入一个错误的处理器,自定义错误的处理逻辑

```java
 /**
     * 获取代理类，该类为 delegate的子类
     * 并且自会重写具有{@link com.zhuguohui.demo.userstate.CheckUserState}注解的方法
     * 自动插入用户状态检测的逻辑
     * @param compositeDisposable 用于取消请求
     * @param context 上下文
     * @param delegate 被代理的类
     * @param errorFunction 出错时的回调
     * @param <T> delegate的类型
     * @return 返回delegate的子类对象
     */
    public static <T> T getProxy(CompositeDisposable compositeDisposable, Context context, T delegate, CallBack<Throwable> errorFunction) {
        return UserStateCheckUtil.getProxy(compositeDisposable,context,delegate,errorFunction);
    }
```

### 效果
只是单纯的弹出提示框

<img src=".\demo-img\取消.gif" style="zoom:50%;" />

# 使用


## 1.添加依赖

该库已经上传到 **jitpack**

```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
添加依赖
```groovy
dependencies {
	        implementation 'com.github.zhuguohui:user-state-check:1.0.0'
	}
```

**具体代码看demo**

## 1.自定义用户状态

示例代码：

重点是要在构造函数中穿入，状态名称，和对应于xml中的属性名称。这样才能和xml属性联动。

```java
public final class DemoUserState extends IUserState {
    private static final DemoUserState login=new DemoUserState("登录",1);
    private static final DemoUserState bindPhoneNumber=new DemoUserState("绑定手机号",2);
    private static final DemoUserState bindRealName=new DemoUserState("实名认证",4);

    public static final int Login=1;
    public static final int BindPhoneNumber=2;
    public static final int BindRealName=4;


    public static final DemoUserState[] values=new DemoUserState[]{login,bindPhoneNumber,bindRealName};
    protected DemoUserState(String desc, int attrFlagValue) {
        super(desc, attrFlagValue);
    }


    public static IUserState[] getUserStateByFlags(int flags) {
        DemoUserState[] values = DemoUserState.values;
        List<DemoUserState> stateList=new ArrayList<>(0);
        for(DemoUserState state:values){
            boolean match =( flags & state.getAttrFlagValue()) == state.getAttrFlagValue();
            if(match){
                stateList.add(state);
            }
        }

        return stateList.toArray(new IUserState[0]);
    }
}
```

## 声明xml属性

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!--用户检查相关的属性，放置在类下方便查找-->
    <!--注释使用flag的属性不能定义format-->
    <attr name="checkUserState">
        <flag name="login" value="1"/>
        <flag name="bindPhone" value="2"/>
        <flag name="bindRealName" value="4"/>
    </attr>

</resources>
```

## 实现自己的状态管理器

```java
/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/3/1
 * Time: 13:27
 * Desc:用户状态管理的接口
 * 定义成接口方便不同的项目实现具体的类
 * </pre>
 */
public  interface  IUserStateManager {
    /**
     * 是否达到了用户状态
     * @param userState
     * @return
     */
    boolean isMatchUserState(IUserState userState);

    /**
     * 执行相应的请求
     * @param state
     * @return
     */
    void doMatchUserState(Context context,IUserState state);

    /**
     * 用于判断 当前的页面的用途，因为每种用户状态的确认可能涉及多个页面
     * 比如登录可能涉及到登录和注册两个页面。只有相关的页面都销毁了。
     * 才可以判断是否登录成功，回调相关的callback
     * @param activity
     * @return 如果当前页面和登录相关返回 用户状态数组
     *         以此类推，如果都不相关，返回null。
     */
    IUserState[] getActivityUserStateType(Activity activity);

    /**
     * 通过flags获取用户状态
     * @param flags
     * @return
     */
    IUserState[] getUserStateByFlags(int flags);
}
```

## 改造BaseActivity 

1.要实现View拦截，需要注入ViewFactory2

2.要实现回调。需要在 **onPostCreate()** 和 **onDestroy()** 方法中回调框架的方法。

```java
public class BaseActivity extends AppCompatActivity {

  private   static Activity currentActivity;
    protected CompositeDisposable compositeDisposable=new CompositeDisposable();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //注入factory2
        LayoutInflaterCompat.setFactory2(LayoutInflater.from(this), UserStateManager.getLayoutInflaterFactory(this,compositeDisposable, e->{
            GZToast.error().show(e.getMessage());
        }));
        super.onCreate(savedInstanceState);
        currentActivity=this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentActivity=this;
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        UserStateManager.getInstance().onActivityPostCreate(this);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserStateManager.getInstance().onActivityDestroy(this);
        compositeDisposable.dispose();
    }
}
```

## 用户状态相关的界面需要实现IUserStatePage接口



```java
package com.zhuguohui.demo.userstate;


import androidx.annotation.Nullable;

/**
 * <pre>
 * Created by zhuguohui
 * Date: 2023/3/1
 * Time: 14:07
 * Desc:通常由activity实现。
 * 表示这个界面和用户的状态的流程有关，比如登录，绑定手机号，实名认证
 * 因为一个流程比如登录，可能涉及到多个页面。比如注册，登录，找回密码
 * 只有相关的页面全部finish以后，才去检查是否登录成功，回调相关的方法。
 * </pre>
 */
public interface IUserStatePage {

    /**
     * 返回当前页面和那些流程相关
     * @return 返回相关用户状态的数组，可以为null
     */
   @Nullable
   IUserState[] getUserSatePageTypes();
}
```

## 将状态管理器设置给框架

```java
/**
 * Created by zhuguohui
 * Date: 2023/3/26
 * Time: 11:18
 * Desc:
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UserStateManager.getInstance().setUserStateManagerImpl(DemoUserStateManager.getInstance());
    }
}
```

具体代码还得看demo



# 原理

## 动态代理



基于开源库

最开始使用的是 下面这个cglib库

[CGLib-for-Android](https://github.com/leo-ouyang/CGLib-for-Android)


但是这个库有个问题,不支持对没有无参构造函数类的动态代理，我还提了Issues

<img src=".\demo-img\提问题.png" style="zoom:50%;" />

后来我发现这个库用的是dexmaker于是，直接使用dexmaker



[dexmaker](https://github.com/linkedin/dexmaker)



dexmaker可以对只有有参构造函数的类实现动态代理。



但是又遇到下一个问题。动态代理view。有些方法不能被代理

如果一个方法被@UnsupportedAppUsage 注解注释。那么无法通过反射获取。

<img src=".\demo-img\不支持的方法.png" style="zoom:50%;" />

而我们要代理onClick方法也不需要重写所有方法

所以我在原有的dexmaker的基础上，添加了可以自定义要重写那个方法的功能。
```java

public final class ProxyBuilder<T> {
    public interface MethodOverrideFilter{
        boolean isOverrideMethod(Method method);
    }

    MethodOverrideFilter methodOverrideFilter;

    public ProxyBuilder<T> setMethodOverrideFilter(MethodOverrideFilter methodOverrideFilter) {
        this.methodOverrideFilter = methodOverrideFilter;
        return this;
    }
}
```
为了便于使用生成了自己的库。

[zhuguohui/Android-Cglib]( https://github.com/zhuguohui/Android-Cglib)



使用该库实现对setOnClickListener的动态代理。

还有个好处，如果不单独重写这一个方法的话。光是LinearLayout中就要800多个public方法需要重写。每个方法都通过反射调用。性能比较差。

```java
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
```

## 用户状态检测

基于rxjava的ObservableTransformer实现，代码如下。

```java
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
```