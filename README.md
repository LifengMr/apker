apker is a plugin framework<br>

step:<br>
host:<br>
1、import apker<br>
2、ProGuard keep class "com.ss.android.apker.helper.IntentBuild"<br>

plugin:<br>
1、root build.gradle: classpath 'com.ss.android.apker.build:apkerbuid:1.2.7'<br>
2、plugin module: apply plugin: 'ss.apkerbuild'<br>
3、use cmd 'gradle clean' and 'gradle aR' to build plugin apk<br>


feature:<br>
1、plugin and host run in the same process<br>
2、plugin and host can use same libs and dependence<br>
3、support so and resource<br>
4、independence signature verification<br>
5、plugin develop without depending apker<br>
6、dynamic install ContentProvider without registering in host<br>
7、little hook and not affect host too much<br>
8、download online<br>
9、and so on<br>


WIKI:<br>
have no time recently, but I will add later.


