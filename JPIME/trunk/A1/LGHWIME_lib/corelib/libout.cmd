@echo off
setlocal

set PROGUARD_PATH=C:\android-sdk-windows\tools\proguard\lib
set PROJECT_LIB=%1
set PROJECT_LIBOUT=%2
set PROJECT_MAIN=%3

set PROGUARD=%PROGUARD_PATH%\proguard.jar

echo copying...

del %PROJECT_MAIN%\assets /f /s /q > NUL
del %PROJECT_LIBOUT%\res /f /s /q > NUL
del %PROJECT_LIBOUT%\TO_MAIN_SOURCE /f /s /q > NUL
del %PROJECT_MAIN%\assets /f /s /q > NUL

echo  %PROJECT_LIB%\res to %PROJECT_LIBOUT%\res
xcopy %PROJECT_LIB%\res    %PROJECT_LIBOUT%\res /e /i /h /y /q > NUL
echo  %PROJECT_LIB%\assets to %PROJECT_MAIN%\assets
xcopy %PROJECT_LIB%\assets    %PROJECT_MAIN%\assets /e /i /h /y /q > NUL
echo  %PROJECT_LIB%\assets to %PROJECT_LIBOUT%\TO_MAIN_SOURCE
xcopy %PROJECT_LIB%\assets    %PROJECT_LIBOUT%\TO_MAIN_SOURCE\assets /e /i /h /y /q > NUL

echo  %PROJECT_MAIN%\src\com\lge\handwritingime to %PROJECT_LIBOUT%\TO_MAIN_SOURCE
xcopy %PROJECT_MAIN%\src\com\lge\handwritingime    %PROJECT_LIBOUT%\TO_MAIN_SOURCE\src\com\lge\handwritingime /e /i /h /y /q > NUL

copy %PROJECT_LIB%\bin\classes\com\lge\handwritingime\r* classes\com\lge\handwritingime\ > NUL
java -jar %PROGUARD% @mk_libout.conf -injars %PROJECT_LIB%\bin\lghwime_lib.jar -outjars %PROJECT_LIBOUT%\libs\lghwime_lib_out.jar
::copy %PROJECT_LIB%\bin\lghwime_lib.jar %PROJECT_LIBOUT%\libs\lghwime_lib_out.jar

echo DONE.
endlocal