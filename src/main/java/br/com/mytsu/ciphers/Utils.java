package br.com.mytsu.ciphers;

public class Utils {

    /**
     * Converte a chave de entrada para um vetor de bytes
     * 
     * @param inputKey chave de entrada
     * @param size tamanho da chave
     * @return vetor de bytes gerado
     */
    public static byte[] makekey(String inputKey, int size) {
        byte[] key = new byte[size];
        int i, j;
        for (j = 0; j < key.length; ++j) {
            key[j] = 0;
        }
        for (i = 0, j = 0; i < inputKey.length(); i++, j = (j + 1) % key.length) {
            key[j] ^= (byte) inputKey.charAt(i);
        }
        return key;
    }

    /**
     * Realiza operação XOR em dois blocos
     * 
     * @param x bloco 1
     * @param offset ponto de início do bloco 1
     * @param y bloco 2
     * @param blockSize tamanho dos blocos
     * @return cópia do bloco 1 depois da operação, sem alterar o bloco original
     */
    public static byte[] xor(byte[] x, int offset, byte[] y, int blockSize) {
        byte[] out = new byte[x.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = x[i];
        }
        for (int i = 0; i < blockSize; i++) {
            out[i] ^= y[i];
        }
        return out;
    }

    /**
     * Concatena 2 inteiros
     * 
     * @param x valor 1
     * @param y valor 2
     * @return valores 1 e 2 concatenados (x | y)
     */
    public static int concat(int x, int y) {
        int b1 = (x & 0xFF) << 8;
        int b2 = y & 0xFF;
        return (b1 | b2);
    }

    /**
     * Concatena 2 vetores de bytes
     * 
     * @param x vetor 1
     * @param y vetor 2
     * @return concatenação dos vetores (x | y)
     */
    public static byte[] concatArray(byte[] x, byte[] y) {
        byte[] out = new byte[x.length + y.length];
        int i = 0;
        for (byte b : x) {
            out[i++] = b;
        }
        for (byte b : y) {
            out[i++] = b;
        }
        return out;
    }
}