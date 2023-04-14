#Relatório A19 - Entrega 3

    Para adotar a arquitetura gossip neste projeto começámos por implementar _vector clocks_ através da adição de um
time stamp vetorial aos servidores e clientes. Assim, quando o cliente faz um pedido a um servidor, envia o seu
TS (time stamp) associado; se este for menor ou igual ao TS do servidor a que é feito esse pedido a operação é
efetuada, avançando-se o TS do servidor por uma unidade e enviando-se esse TS atualizado ao cliente para este
atualizar o seu também. Nesta etapa, no caso de o TS do cliente ser superior ao do servidor apenas se lançava uma mensagem 
de erro ao cliente, não sendo executada a operação.
    Em seguida procurámos resolver o problema de operações não executadas. Adicionámos às operações um TS (TS do servidor na 
altura em que foi recebida), um prevTS (TS do pedido do cliente) e um booleano para identificar se são estáveis ou 
não, ou seja, se foi possível executá-las de acordo com os TS envolvidos são estáveis, caso contrário são guardadas na ledger do 
servidor como instáveis, para ser possível realizá-las no futuro quando o TS do server for superior ao prevTS da operação.
    Implementámos a funcionalidade de gossip, em que quando chamada o servidor passado como argumento envia a sua ledger
de operações (estáveis e instáveis) e TS para os outros servidores. Ao receber isto, os outros servidores fazem propagate
state, isto é, mudam o seu TS para o recebido se o novo for maior, percorrem as operações da ledger recebida, executando-as
se estas forem estáveis e diferentes das da sua própria ledger, adicionando as que não forem repetidas, incluindo as
instáveis recebidas, à sua ledger. Depois, além disso, também percorre a sua ledger atualizada, verificando se as operações
marcadas como instáveis passaram a ser estáveis, caso no qual as executa.
    Adicionalmente, fizémos possível haver um terceiro servidor C.