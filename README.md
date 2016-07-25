apker is a plugin framework

step:
host:
1、import apker \n
2、ProGuard keep class "com.ss.android.apker.helper.IntentBuild"

plugin:
1、root build.gradle: classpath 'com.ss.android.apker.build:apkerbuid:1.2.7'
2、plugin module: apply plugin: 'ss.apkerbuild'
3、use cmd 'gradle clean' and 'gradle aR' to build plugin apk


feature:
1、plugin and host run in the same process
2、plugin and host can use same libs and dependence
3、support so and resource
4、independence signature verification
5、plugin develop without depending apker
6、dynamic install ContentProvider without registering in host
7、little hook and not affect host too much
8、download online
9、and so on



notice:
1、if plugin use the same lib with host, the lib's version should be same with host too



TODO:
1、optimize gradle pack code(filter code which is same with host)
2、support plugin proguard
3、filter device which is not support plugin（control online）

