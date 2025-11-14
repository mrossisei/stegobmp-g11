#!/bin/bash


LAUNCHER="./stegobmp"
PORTADOR="resources/ejemplo/lado.bmp"
SECRETO="secreto_de_prueba.txt"
OUTPUT_DIR="output_pruebas"
PASSWORD="superclave123"

STEG_ALGOS=("LSB1" "LSB4" "LSBI")
CRYPT_ALGOS=("aes128" "aes192" "aes256" "3des")
CRYPT_MODES=("cbc" "ecb" "cfb" "ofb")


echo "🧹 Limpiando pruebas anteriores en $OUTPUT_DIR..."
mkdir -p $OUTPUT_DIR
rm -f $OUTPUT_DIR/*

echo "--- Archivo secreto '$SECRETO' creado. ---"
echo "Este es un mensaje secreto para el TP de Cripto y Seguridad." > $SECRETO
echo ""


echo "--- 🧪 Iniciando Pruebas SIN Encripción ---"

for STEG in "${STEG_ALGOS[@]}"; do
    echo "▶️ Probando: $STEG (Embed)"
    OUT_FILE="$OUTPUT_DIR/salida_${STEG}.bmp"
    EXT_FILE="$OUTPUT_DIR/extraido_${STEG}"

    $LAUNCHER -embed -in $SECRETO -p $PORTADOR -out $OUT_FILE -steg $STEG

    echo "▶️ Probando: $STEG (Extract)"
    $LAUNCHER -extract -p $OUT_FILE -out $EXT_FILE -steg $STEG


    diff $SECRETO "${EXT_FILE}.txt"
    if [ $? -eq 0 ]; then
        echo "✅ $STEG: OK"
    else
        echo "❌ $STEG: FALLÓ (Revisar ${EXT_FILE}.txt)"
    fi
    echo ""
done


echo "--- 🧪 Iniciando Pruebas CON Encripción (Matriz Completa) ---"

for STEG in "${STEG_ALGOS[@]}"; do
    for ALGO in "${CRYPT_ALGOS[@]}"; do
        for MODE in "${CRYPT_MODES[@]}"; do

            TEST_NAME="${STEG}_${ALGO}_${MODE}"
            echo "▶️ Probando: $TEST_NAME"

            OUT_FILE="$OUTPUT_DIR/salida_${TEST_NAME}.bmp"
            EXT_FILE="$OUTPUT_DIR/extraido_${TEST_NAME}"


            $LAUNCHER -embed -in $SECRETO -p $PORTADOR -out $OUT_FILE \
                      -steg $STEG -a $ALGO -m $MODE -pass $PASSWORD


            $LAUNCHER -extract -p $OUT_FILE -out $EXT_FILE \
                      -steg $STEG -a $ALGO -m $MODE -pass $PASSWORD


            diff $SECRETO "${EXT_FILE}.txt"
            if [ $? -eq 0 ]; then
                echo "✅ $TEST_NAME: OK"
            else

                echo "❌ $TEST_NAME: FALLÓ (Revisar ${EXT_FILE}.txt)"
            fi
            echo ""
        done
    done
done

echo "-----------------------------------------------------"
echo "🏁 Pruebas completadas."
echo "Tus archivos de salida están en la carpeta: $OUTPUT_DIR"