# Algoritmo de Cifra IDEA

Este é uma resenha traduzida (apenas a introdução e descrição do algoritmo foi abordada) do artigo de _Nick Hoffman_. Você pode encontrar o artigo através [deste link](https://www.nku.edu/~christensen/simplified%20IDEA%20algorithm.pdf).

## Sobre

O _International Data Encryption Algorithm_ é uma cifra de bloco simétrica. Publicada em 1991 por Lai, Messey e Murphy.

## Descrição

IDEA encripta blocos de 64-bits de texto plano em blocos de 64-bits de texto cifrado. Ele utiliza uma chave de 128-bits. O algoritmo consiste em 8 rodadas idênticas e "meia" rodada final de transformação.

A ideia algébrica do IDEA é a mistura de três operações algébricas incompatíveis em blocos de 16-bits: _Bitwise XOR_, adição do módulo 2<sup>16</sup>, e multiplicação do módulo 2<sup>16</sup> + 1.

Há 2<sup>16</sup> possíveis blocos de 16-bits: 0000000000000000, ..., 1111111111111111, que representam os inteiros 0, ..., 2<sup>16</sup> - 1. Cada operação com o conjunto de possíveis blocos de 16-bits é um grupo algébrico. _Bitwise XOR_ é adição do módulo de 2 bit a bit, e adição do módulo 2<sup>16</sup> é a operação de agrupamento usual. Algum giro deve ser colocado nos elementos -- os blocos de 16-bits -- para fazer sentido da multiplicação do módulo 2<sup>16</sup> + 1.

Para a descrição do IDEA, quebramos o algoritmo de encriptação em quatorze passos. Para cada uma das oito rodadas completas, o texto plano de 64-bits é dividido em 4 sub blocos de 16-bits: X<sub>1</sub>, X<sub>2</sub>, X<sub>3</sub>, X<sub>4</sub>. O bloco de entrada de 64-bits é a concatenação dos sub blocos X<sub>1</sub> || X<sub>2</sub> || X<sub>3</sub> || X<sub>4</sub>, onde || denota concatenação. Cada rodada completa requer seis sub chaves. A chave de 128-bits é dividida em oito blocos de 16-bits, virando oito sub chaves. As primeiras seis sub chaves são usadas na rodada 1, e as duas restantes são usadas na rodada 2.

Cada rodada usa cada uma das três operações algébricas: _Bitwise XOR_, adição do módulo 2<sup>16</sup> e multiplicação do módulo 2<sup>16</sup> + 1.

Aqui estão os quatorze passos para uma rodada completa (_multiplica_ significa a multiplicação do módulo 2<sup>16</sup> + 1, e _soma_ significa a adição do módulo 2<sup>16</sup>):

1. _Multiplica X<sub>1</sub>_ e a primeira sub-chave _Z<sub>1</sub>_.
2. _Soma X<sub>2</sub>_ e a segunda sub-chave _Z<sub>2</sub>_.
3. _Soma X<sub>3</sub>_ e a terceira sub-chave _Z<sub>3</sub>_.
4. _Multiplica X<sub>4</sub>_ e a quarta sub-chave _Z<sub>4</sub>_.
5. _Bitwise XOR_ dos resultados dos passos 1 e 3.
6. _Bitwise XOR_ dos resultados dos passos 2 e 4.
7. _Multiplica_ o resultado do passo 5 e a quinta sub-chave _Z<sub>5</sub>_.
8. _Soma_ os resultados dos passos 6 e 7.
9. _Multiplica_ o resultado do passo 8 e a sexta sub-chave _Z<sub>6</sub>.
10. _Soma_ os resultados dos passos 7 e 9.
11. _Bitwise XOR_ dos resultados dos passos 1 e 9.
12. _Bitwise XOR_ dos resultados dos passos 3 e 9.
13. _Bitwise XOR_ dos resultados dos passos 2 e 10.
14. _Bitwise XOR_ dos resultados dos passos 4 e 10.

Para cada rodada exceto a transformação final, um _swap_ ocorre, e a entrada para a próxima rodada é: resultado do passo 11 || resultado do passo 13 || resultado do passo 12 || resultado do passo 14, o que se torna X<sub>1</sub> || X<sub>2</sub> || X<sub>3</sub> || X<sub>4</sub>, para a entrada da próxima rodada.

Após a rodada 8, uma nona transformação de "meia rodada" ocorre:

1. _Multiplica X<sub>1</sub>_ e a primeira sub-chave.
2. _Soma X<sub>2</sub>_ e a segunda sub-chave.
3. _Soma X<sub>3<sub>_ e a terceira sub-chave.
4. _Multiplica X<sub>4</sub>_ e a quarta sub-chave.

A concatenação dos blocos é a saída.

## Agendamento das chaves

Cada uma das oito rodadas requer seis sub-chaves, e a transformação final "meia rodada" requer quatro sub-chaves; portanto, o processo inteiro requer 52 sub-chaves.

A chave 128-bits é dividida em oito sub-chaves de 16-bits. Então os bits são deslocados para a esquerda 25-bits. O resultado que é uma _string_ de 128-bits é dividida em oito blocos de 16-bits que se tornarão as próximas oito sub-chaves. O processo de deslocamento é realizado até que todas as 52 sub-chaves são geradas.

Os deslocamentos de 25-bits garantem que a repetição não ocorra das sub-chaves.

Seis sub-chaves são usadas em cada uma das 8 rodadas. As 4 sub-chaves finais são usadas na nona transformação "meia rodada".