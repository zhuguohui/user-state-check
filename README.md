# user-state-check
基于AOP实现用户状态检测的框架



# 功能

1.通过dexmaker 实现AOP，通过设置ViewFactory2，动态生成view的子类。配合xml中定义属性。可以无感的拦截任意view的点击事件

2.通过dexMaker 实现AOP，可以生成任意类的子类。便于和viewDataBing联合使用。

3.可以和RxJava联合使用。

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

设置需要检测的状态为，登录，绑定手机号，实名认证。

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

正常设置点击事件即可，想过的拦截工作对业务代码无感。

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

会抛出**UserStateCheckException**通过其**getState()**方法可以获取匹配不成功的用户状态

在AOP相关类的时候都可以传入一个错误的处理器,自定义错误的处理逻辑

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


## 添加依赖

该库已经上传只jitpack

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
