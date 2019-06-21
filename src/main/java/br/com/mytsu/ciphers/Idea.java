package br.com.mytsu.ciphers;

public class Idea {
    private static final int KEY_SIZE = 16;
    private static final int BLOCK_SIZE = 8;
    private static final int ROUNDS = 8;
    private boolean encrypt;
    private int[] subkey;

    public Idea(byte[] key) {
        this.encrypt = true;
        this.setkey(key);
    }

    public Idea(byte[] key, boolean encrypt) {
        this.encrypt = encrypt;
        this.setkey(key);
    }

    /**
     * Inverte as sub-chaves para obter as chaves de desencriptação Elas são
     * inversas tanto aditivas quanto multiplicativas das sub-chaves de encriptação
     * em ordem reversa.
     * 
     * @param key
     * @return
     */
    private static int[] invertSubkey(int[] key) {
        int[] invKey = new int[key.length];
        int p = 0;
        int i = ROUNDS * 6;
        invKey[i + 0] = mulInv(key[p++]);
        invKey[i + 1] = addInv(key[p++]);
        invKey[i + 2] = addInv(key[p++]);
        invKey[i + 3] = mulInv(key[p++]);
        for (int r = ROUNDS - 1; r >= 0; r--) {
            i = r * 6;
            int m = r > 0 ? 2 : 1;
            int n = r > 0 ? 1 : 2;
            invKey[i + 4] = key[p++];
            invKey[i + 5] = key[p++];
            invKey[i + 0] = mulInv(key[p++]);
            invKey[i + m] = addInv(key[p++]);
            invKey[i + n] = addInv(key[p++]);
            invKey[i + 3] = mulInv(key[p++]);
        }
        return invKey;
    }

    /**
     * Adição, respeitando o tamanho de MAX_RANGE (mod 2^16) range [0, 0xFFFF].
     * 
     * @param x valor 1
     * @param y valor 2
     * @return resultado da soma com o módulo
     */
    private static int add(int x, int y) {
        return (x + y) & 0xFFFF;
    }

    /**
     * Adição inversa, respeitando o tamanho de MAX_RANGE (mod 2^16) range [0,
     * 0xFFFF].
     * 
     * @param i entrada
     * @return resultado da soma inversa
     */
    private static int addInv(int i) {
        return (0x10000 - i) & 0xFFFF;
    }

    /**
     * Multiplicação, respeitando o tamanho + 1 (mod 2^16 + 1 = mod 0x10001). range
     * [0, 0xFFFF].
     * 
     * @param x entrada 1
     * @param y entrada 2
     * @return resultado da multiplicação
     */
    private static int mul(int x, int y) {
        long m = (long) x * y;
        if (m != 0) {
            return (int) (m % 0x10001) & 0xFFFF;
        } else {
            if (x != 0 || y != 0) {
                return (1 - x - y) & 0xFFFF;
            }
            return 1;
        }
    }

    /**
     * Multiplicador inverso do grupo multiplicativo (mod 2^16+1). Ele utiliza o
     * algoritmo extendido euclidiano (Veja:
     * https://en.wikipedia.org/wiki/Extended_Euclidean_algorithm) para encontrar o
     * inverso. Para os propósitos do IDEA, os blocos zerados são considerados para
     * representar 2^16 = -1 para a multiplicação do módulo 2^16 + 1; assim o
     * inverso multiplicativo de 0 é 0. range [0, 0xFFFF].
     * 
     * @param x entrada
     * @return inverso multiplicativo da entrada
     */
    private static int mulInv(int x) {
        if (x <= 1) {
            return x; // 0 e 1 são seus próprios inversos
        }

        try {
            int y = 0x10001, t0 = 1, t1 = 0;
            while (true) {
                t1 += y / x * t0;
                y %= x;
                if (y == 1) {
                    return (1 - t1) & 0xFFFF;
                }
                t0 += x / y * t1;
                x %= y;
                if (x == 1) {
                    return t0;
                }
            }
        } catch (ArithmeticException e) {
            return 0;
        }
    }

    /**
     * Cria as sub-chaves a partir da chave provida pelo usuário
     * 
     * @param inputkey chave de 128-bits
     * @return sub-chaves de 16-bits (seis para cada rodada e quatro para a última
     *         meia rodada)
     */
    private int[] generateSubKeys(byte[] inputkey) {
        if (inputkey.length != KEY_SIZE) {
            System.err.println("Tamanho de chave inválido!");
            System.err.println("Tamanho atual da chave: " + inputkey.length);
            throw new IllegalArgumentException();
        }
        int[] key = new int[ROUNDS * 6 + 4]; // Todas as sub-chaves necessárias

        // Dividindo a chave de 128-bits em oito blocos de 16-bits
        int b1, b2;
        for (int i = 0; i < inputkey.length / 2; i++) {
            key[i] = Utils.concat(inputkey[2 * i], inputkey[2 * i + 1]);
        }

        // A chave é deslocada em 25 bits para a esquerda e novamente dividida em oito
        // sub-chaves
        // As primeiras quatro são usadas na rodada 2, e as últimas quatro na rodada 3.
        // A chave é deslocada em mais 25 bits para a esquerda para as próximas oito
        // chaves,
        // e assim em diante.
        for (int i = inputkey.length / 2; i < key.length; i++) {
            // Ele começa combinando k1 deslocado 9 bits com k2, isto é 16 bits de k0 + 9
            // bits
            // deslocados de k1 = 25 bits.
            b1 = key[(i + 1) % 8 != 0 ? i - 7 : i - 15] << 9;
            b2 = key[(i + 2) % 8 < 2 ? i - 14 : i - 6] >>> 7;
            key[i] = (b1 | b2) & 0xFFFF;
        }
        return key;
    }

    /**
     * Realiza a encriptação/desencriptação dos dados a partir da posição indicada
     * OBS: Não altera os dados originais de entrada
     * 
     * @param data   dados de entrada
     * @param offset posição de início do processo
     * @return dados encriptados/desencriptados
     */
    public byte[] crypt(byte[] data, int offset) {
        // Criando um novo vetor de dados para manter a pureza da função
        byte[] out = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            out[i] = data[i];
        }
        // Divide os blocos de 64-bits em quatro sub-blocos de 16-bits
        int x1 = Utils.concat(data[offset + 0], data[offset + 1]);
        int x2 = Utils.concat(data[offset + 2], data[offset + 3]);
        int x3 = Utils.concat(data[offset + 4], data[offset + 5]);
        int x4 = Utils.concat(data[offset + 6], data[offset + 7]);
        int k = 0; // index de this.subkey
        for (int round = 0; round < ROUNDS; round++) {
            int y1 = mul(x1, this.subkey[k++]);
            int y2 = add(x2, this.subkey[k++]);
            int y3 = add(x3, this.subkey[k++]);
            int y4 = mul(x4, this.subkey[k++]);
            int y5 = y1 ^ y3;
            int y6 = y2 ^ y4;
            int y7 = mul(y5, this.subkey[k++]);
            int y8 = add(y6, y7);
            int y9 = mul(y8, this.subkey[k++]);
            int y10 = add(y7, y9);
            x1 = y1 ^ y9;
            x2 = y3 ^ y9;
            x3 = y2 ^ y10;
            x4 = y4 ^ y10;
        }
        // Transformação final da saída
        int r0 = mul(x1, this.subkey[k++]);
        int r1 = add(x3, this.subkey[k++]);
        int r2 = add(x2, this.subkey[k++]);
        int r3 = mul(x4, this.subkey[k]);
        // Concatenando os sub-blocos
        out[offset + 0] = (byte) (r0 >> 8);
        out[offset + 1] = (byte) r0;
        out[offset + 2] = (byte) (r1 >> 8);
        out[offset + 3] = (byte) r1;
        out[offset + 4] = (byte) (r2 >> 8);
        out[offset + 5] = (byte) r2;
        out[offset + 6] = (byte) (r3 >> 8);
        out[offset + 7] = (byte) r3;
        return out;
    }

    /**
     * Seta a chave para encriptação, ou o inverso da chave para desencriptação
     * 
     * @param key
     */
    void setkey(byte[] key) {
        int[] tempkey = this.generateSubKeys(key);
        if (this.encrypt) {
            this.subkey = tempkey;
        } else {
            this.subkey = invertSubkey(tempkey);
        }
    }

    /**
     * Realiza encriptação a partir do início dos dados
     * 
     * @param data dados em formato de texto plano
     * @return dados cifrados
     */
    public byte[] crypt(byte[] data) {
        return this.crypt(data, 0);
    }

    /**
     * @return tamanho do bloco de dados
     */
    public static int getBlockSize() {
        return BLOCK_SIZE;
    }

    /**
     * @return tamanho da chave
     */
    public static int getKeySize() {
        return KEY_SIZE;
    }
}