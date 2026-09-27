@echo off
echo ==================================================
echo   MENJALANKAN FUTSAL & MINI SOCCER ARENA MALANG
echo ==================================================

if not exist "bin\com\futsal\Main.class" (
    echo Mengompilasi program terlebih dahulu...
    if not exist "bin" mkdir "bin"
    javac -encoding UTF-8 -d bin src/com/futsal/util/*.java src/com/futsal/model/*.java src/com/futsal/service/*.java src/com/futsal/gui/*.java src/com/futsal/*.java
)

java -cp bin com.futsal.Main

