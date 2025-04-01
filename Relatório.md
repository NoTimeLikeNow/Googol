Client.java
Este código implementa um cliente em um sistema de busca distribuído utilizando o JAVA RMI (Remote Method Invocation). De modo a interagir com um servidor remoto que deve registrado no RMI Registry para futura busca de keywords em páginas da internet e também faz a utilização da biblioteca JSoup para processamento das paginas em HTML e extrair palavras e links.


Downloaders.java 
O código referente aos Downloaders estabelece através de um crawler distribuído recolhendo dados para um servidor de indexação e pertence a um sistema de busca utilizando Java RMI para se comunicar de forma remota. 

Gate.java e Index.java são interfaces rmi 
Com a finalidade de estabelecer os métodos remotos utilizados para comunicação entre os elementos de busca distribuídas a interface Gate, nesta utiliza-se o Remote para que os métodos sejam chamado de forma remota. Esta interface age como meio de correlação entre os servidores que as implementam e os sistemas que invocam os seus metódos.

Gateway.java
O código referente ao Gateway integra o sistema de busca usando Java RMI, com o intuito de gerir os crawlers, o cliente e os servidores de indexação, mantem comunicação entre todas as divisões para armazenar e buscar os dados