@echo off
echo ==================================================
echo   MENGOMPILASI FUTSAL & MINI SOCCER ARENA MALANG
echo ==================================================

if not exist "bin" mkdir "bin"

javac -encoding UTF-8 -d bin src/com/futsal/util/*.java src/com/futsal/model/*.java src/com/futsal/service/*.java src/com/futsal/gui/*.java src/com/futsal/*.java src/com/futsal/test/*.java

if %ERRORLEVEL% EQU 0 (
    echo [SUKSES] Kompilasi berhasil! Berkas .class tersimpan di folder bin/
) else (
    echo [GAGAL] Terjadi kesalahan saat kompilasi.
)
pause
