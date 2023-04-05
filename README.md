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
