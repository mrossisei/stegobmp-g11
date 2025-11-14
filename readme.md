# 🖼️ Stegobmp -- Trabajo Práctico de Esteganografía G11

**Criptografía y Seguridad (72.04) -- ITBA**

Implementación del Trabajo Práctico de Esteganografía (**stegobmp**)
para la materia *Criptografía y Seguridad*.\
El programa permite ocultar (`-embed`) y extraer (`-extract`) archivos
dentro de imágenes **BMP de 24 bits sin compresión**, utilizando los
algoritmos **LSB1, LSB4 y LSBI**.\
Además, soporta encripción del payload con **AES (128, 192, 256)** y
**3DES** en modos **CBC, ECB, CFB y OFB**.

## 📋 Requisitos Previos

-   **Java 17 (JDK)**
-   **Apache Maven**
-   **Entorno Linux** (recomendado: WSL2 con Ubuntu 22.04.3 LTS)

## ⚙️ Compilación

El proyecto utiliza **Maven** para la gestión de dependencias y la
compilación.\
El `pom.xml` está configurado para generar un **fat JAR** (ejecutable
con todas las dependencias incluidas).

1.  Abrí una terminal en la carpeta raíz del proyecto (donde está el
    `pom.xml`).

2.  Ejecutá:

    ``` bash
    mvn clean package
    ```

3.  Maven generará el ejecutable en:

        target/stegobmp-1.0-SNAPSHOT-jar-with-dependencies.jar

## ▶️ Ejecución

El proyecto incluye un script (`stegobmp`) para facilitar la
ejecución del `.jar`.

1.  **Dar permisos de ejecución**:

    ``` bash
    chmod +x stegobmp
    ```

2.  **Ejecutar el programa**:

    ``` bash
    ./stegobmp [parametros]
    ```

## 📌 Sintaxis de Uso

### 🔵 Ocultar (Embed)

``` bash
./stegobmp -embed -in <archivo_secreto> -p <portador.bmp> -out <salida.bmp> -steg <LSB1|LSB4|LSBI> [opciones_crypto]
```

### 🔵 Extraer (Extract)

``` bash
./stegobmp -extract -p <portador_con_secreto.bmp> -out <archivo_salida> -steg <LSB1|LSB4|LSBI> [opciones_crypto]
```

## 🔐 Opciones de Criptografía (Opcional)

-   **Algoritmo**

        -a <aes128|aes192|aes256|3des>

-   **Modo**

        -m <ecb|cbc|cfb|ofb>

-   **Password**

        -pass <password>

## 🧰 Ejemplos de Uso

### Ejemplo 1 --- Ocultar sin encripción (LSBI)

``` bash
./stegobmp -embed -in "mensaje.txt" -p "resources/ejemplo/lado.bmp" -out "output_pruebas/salida_lsbi.bmp" -steg LSBI
```

### Ejemplo 2 --- Ocultar con encripción (3DES + CBC)

``` bash
./stegobmp -embed -in "mensaje.txt" -p "resources/ejemplo/lado.bmp" -out "output_pruebas/salida_3des.bmp" -steg LSBI -a 3des -m cbc -pass "oculto"
```

### Ejemplo 3 --- Extraer con encripción

``` bash
./stegobmp -extract -p "output_pruebas/salida_3des.bmp" -out "output_pruebas/mensaje_recuperado" -steg LSBI -a 3des -m cbc -pass "oculto"
```

## 🧪 Pruebas (Testing)

El proyecto incluye un script para ejecutar una batería completa de
pruebas, probando todas las combinaciones de algoritmos y encriptación.

1.  Dar permisos de ejecución:

    ``` bash
    chmod +x tests.sh
    ```

2.  Ejecutar la suite:

    ``` bash
    ./tests.sh
    ```
