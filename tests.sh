#!/bin/bash


LAUNCHER="./stegobmp.sh"


PORTADOR="resources/ejemplo/lado.bmp"
SECRETO="secreto_de_prueba.txt"


OUTPUT_DIR="output_pruebas"
mkdir -p $OUTPUT_DIR || echo "Error creando el directorio $OUTPUT_DIR"


OUT_LSB1="./$OUTPUT_DIR/salida_lsb1.bmp"
OUT_LSB4="./$OUTPUT_DIR/salida_lsb4.bmp"
OUT_LSBI="./$OUTPUT_DIR/salida_lsbi.bmp"
OUT_AES="./$OUTPUT_DIR/salida_aes.bmp"


EXT_LSB1="./$OUTPUT_DIR/extraido_lsb1"
EXT_LSB4="./$OUTPUT_DIR/extraido_lsb4"
EXT_LSBI="./$OUTPUT_DIR/extraido_lsbi"
EXT_AES="./$OUTPUT_DIR/extraido_aes"


echo "Este es un mensaje secreto para el TP de Cripto y Seguridad." > $SECRETO
echo "--- Archivo secreto '$SECRETO' creado. ---"



echo ""
echo "▶️ Iniciando Prueba 1: Embed LSB1..."
$LAUNCHER -embed -in $SECRETO -p $PORTADOR -out $OUT_LSB1 -steg LSB1


echo "▶️ Iniciando Prueba 2: Extract LSB1..."
$LAUNCHER -extract -p $OUT_LSB1 -out $EXT_LSB1 -steg LSB1



echo ""
echo "▶️ Iniciando Prueba 3: Embed LSB4..."
$LAUNCHER -embed -in $SECRETO -p $PORTADOR -out $OUT_LSB4 -steg LSB4


echo "▶️ Iniciando Prueba 4: Extract LSB4..."
$LAUNCHER -extract -p $OUT_LSB4 -out $EXT_LSB4 -steg LSB4



echo ""
echo "▶️ Iniciando Prueba 5: Embed LSBI..."
$LAUNCHER -embed -in $SECRETO -p $PORTADOR -out $OUT_LSBI -steg LSBI


echo "▶️ Iniciando Prueba 6: Extract LSBI..."
$LAUNCHER -extract -p $OUT_LSBI -out $EXT_LSBI -steg LSBI


echo ""
echo "▶️ Iniciando Prueba 7: Embed AES128-CBC..."
$LAUNCHER -embed -in $SECRETO -p $PORTADOR -out $OUT_AES -steg LSB1 -pass "password123"

echo "▶️ Iniciando Prueba 8: Extract AES128-CBC..."
$LAUNCHER -extract -p $OUT_AES -out $EXT_AES -steg LSB1 -pass "password123"



echo ""
echo "-----------------------------------------------------"
echo "🏁 Pruebas completadas. Verificando integridad..."
echo "-----------------------------------------------------"

echo "Verificando LSB1..."
diff $SECRETO "$EXT_LSB1.txt"

echo "Verificando LSB4..."
diff $SECRETO "$EXT_LSB4.txt"

echo "Verificando LSBI..."
diff $SECRETO "$EXT_LSBI.txt"

echo "Verificando AES..."
diff $SECRETO "$EXT_AES.txt"

echo ""
echo "✅ --- Verificación finalizada. Si no viste mensajes de 'diferencias', todo salió perfecto. ---"
echo "Tus archivos de salida están en la carpeta: $OUTPUT_DIR"