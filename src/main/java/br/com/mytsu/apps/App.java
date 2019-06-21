package br.com.mytsu.apps;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Scanner;

import br.com.mytsu.ciphers.Idea;

/**
 * Aplicação que realiza a leitura de uma frase dada pelo usuário
 * encripta a frase, e em seguida desencripta utilizando o algoritmo IDEA
 */
public class App 
{

    private static byte[] key = new byte[16];
    private static Idea idea;

    public static void main( String[] args ) {

        new SecureRandom().nextBytes(key);

        byte[] data = new byte[64];
        
        System.out.println("Exemplo de funcionamento do algoritmo IDEA \n");
        System.out.println("Chave utilizada: " + new String(key));
        System.out.println("Digite uma frase: ");
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        sc.close();
        
        data = Arrays.copyOf(input.getBytes(), 64);

        idea = new Idea(key);
        byte[] encryptedData = idea.crypt(data);
        
        idea = new Idea(key, false);

        byte[] decryptedData = idea.crypt(encryptedData);

        // Os dados encriptados não serão decodificados pois pode causar bugs
        //      no terminal.
        System.out.println("Frase Encriptada: " + encryptedData.toString());
        System.out.println("Frase Desencriptada: " + new String(decryptedData));
    }
}

