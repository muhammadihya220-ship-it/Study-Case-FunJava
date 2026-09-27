@echo off
echo ==================================================
echo   MENJALANKAN PENGUJIAN LOGIKA & VALIDASI
echo ==================================================

if not exist "bin" mkdir "bin"
javac -encoding UTF-8 -d bin src/com/futsal/util/*.java src/com/futsal/model/*.java src/com/futsal/service/*.java src/com/futsal/test/*.java

java -cp bin com.futsal.test.TestReservasiFutsal
pause
