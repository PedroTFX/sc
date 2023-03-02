# sc

lista tarefas a fazer:

registar users

log in users

add <wine> <image> - adiciona um novo vinho identificado por wine, associado à imagem
image. Caso já exista um vinho com o mesmo nome deve ser devolvido um erro.
Inicialmente o vinho não terá qualquer classificação e o número de unidades disponíveis
será zero.


• sell <wine> <value> <quantity> - coloca à venda o número indicado por quantity de
unidades do vinho wine pelo valor value. Caso o wine não exista, deve ser devolvido um
erro.


• view <wine> - obtém as informações associadas ao vinho identificado por wine,
nomeadamente a imagem associada, a classificação média e, caso existam unidades do
vinho disponíveis para venda, a indicação do utilizador que as disponibiliza, o preço e a
quantidade disponível. Caso o vinho wine não exista, deve ser devolvido um erro.


• buy <wine> <seller> <quantity> - compra quantity unidades do vinho wine ao utilizador
seller. O número de unidades deve ser removido da quantidade disponível e deve ser
transferido o valor correspondente à compra da conta do comprador para o vendedor.
Caso o vinho não exista, ou não existam unidades suficientes, ou o comprador não tenha
saldo suficiente, deverá ser devolvido e assinalado o erro correspondente.


• wallet - obtém o saldo atual da carteira.


• classify <wine> <stars> - atribui ao vinho wine uma classificação de 1 a 5, indicada por stars.
Caso o vinho wine não exista, deve ser devolvido um erro.


• talk <user> <message> - permite enviar uma mensagem privada ao utilizador user (por
exemplo, uma pergunta relativa a um vinho à venda). Caso o utilizador não exista, deve
ser devolvido um erro.


• read - permite ler as novas mensagens recebidas. Deve ser apresentada a identificação do
remetente e a respetiva mensagem. As mensagens são removidas da caixa de mensagens
do servidor depois de serem lidas.
