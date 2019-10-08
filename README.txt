SIMULADOR DE REDES DE FILAS

Para usar o simulador compilar e rodar o programa App

>javac *.java
>java App

O programa vai ler o sistema de filas no arquivo "queues" e simular de acordo com as especificações de simulação contidas naquele arquivo.

É possível ler um aruivo diferente passando o nome do arquivo por linha de comando

>java App queues2

O arquivo "queues" está comentado e ensina como funciona a sintaxe para definir uma rede de filas. O arquivo queues2 possui um segundo exemplo de rede de filas.

A principal classe de simulação é a QueueSim, contendo quase toda a lógica de simulação.

Objetos da classe QueueStructure guardam a especificação de uma fila.

A classe ScheduleEntry é usada pelo simulador para representar eventos escalonados.

A classe SimulationReport é usada pelo simulador para guardar os resultados de uma simulação.

RNG é a classe do gerador de números aleatórios.