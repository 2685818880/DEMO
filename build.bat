@echo off
title VUE项目编译 - V20200325.1 - 中鼎集成
echo.
::获取当前路径
set root=%cd%
echo 当前路径:%root%
echo.
::将路径的斜杠"\"以逗号替换
::获取项目名称
for  %%I in (%root:\=, %) do (
	set project=%%I
)
echo 项目名称:%project%
echo.
set dist=%root%\vue\dist
if exist %dist% (
	echo 清理%root%\vue\dist
	rmdir %root%\vue\dist /s/q
)
echo.
::切换到vue路径
cd vue
::编译vue文件夹
start npm run build 
set /p="等待编译完成"<nul
:wait
ping -n 2 127.0.0.1>nul
color 3F
if exist %dist%\index.html (
	::do nothing
	echo 编译完成
) else (
	set /p="."<nul
	ping -n 2 127.0.0.1>nul
	color 8B
	goto :wait
)
color 07
echo.
::返回上一级
cd ..
::切换到发布文件夹
cd %dist%
::获取发布文件夹的名称
for /f %%i in ('dir /b /ad') do (set distFolder=%%i)
::echo %distFolder%

::资源文件路径
set resources=%root%\src\main\resources\
::静态资源路径
set static=%resources%static\%distFolder%\
::视图资源路径
set view=%resources%view\%distFolder%\index\
if exist %static% (
	echo 清理%static%
	echo.
	rmdir %static% /s/q
)
if exist %view% (
	echo 清理%view%
	echo.
	rmdir %view% /s/q
)
::创建静态资源路径
echo 资源文件路径：%resources%
echo.
echo 静态资源路径：%static%
echo.
echo 视图资源路径：%view%
echo.
mkdir %static%
mkdir %view%
echo.
cd %root%
xcopy vue\dist\%distFolder% %static% /s/e/i/y
echo.

java -jar build.jar %static% %view% %distFolder%
echo.
explorer.exe /select,%resources%
pause
::结束CMD程序
taskkill /IM cmd.exe