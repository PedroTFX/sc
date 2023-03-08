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

Acho que é excelente ideia fornecer uma API consistente que o servidor possa usar com Request e Response:
```java
class Request implements Serializable {
	enum Type { AUTH, ADD, SELL, VIEW, BUY, WALLET, CLASSIFY, TALK, READ, QUIT }

	Type type;
	Object payload;

	Request(Type type, Object payload) {
		this.type = type;
		this.payload = payload;
	}

	class Auth {
		String user;
		String password;
	}

	class Add {
		String wine;
		Image image;
	}

	// ...
}

class Response implements Serializable {
	enum Type { OK, ERROR, WALLET, MAIL }

	Type type;
	Object payload;

	class Error {
		String message;
	}

	class Wallet {
		int balance;
	}

	class Mail {
		Hashtable<String, String[]> messages;
	}
}
```

Fazemos um pedido:
```java
Request request = new Request(Request.Type.Auth, new Request.Auth(user, password));
out.writeObject(request);
```
E recebemos a resposta:
```java
Response response = in.readObject();
Type type = response.type;
Object payload = response.payload;

if(type == Response.Type.ERROR) {
	System.out.println(((Error) payload).message);
} else if(type == Response.Type.OK) {
	// Correu tudo bem
} else if(type == Response.Type.WALLET) {
	System.out.println(((Wallet) payload).balance);
} // ...

Request request = (Request) inStream.readObject();
if(request.type == Request.Type.AUTH) {
	Auth auth = (Auth) request.payload;
	authentication(auth.user, auth.password);
}
```

No servidor, fazemos isto:
```java


Request request = (Request) inStream.readObject();
Type type = request.type;
Object payload = request.payload;
if(type == response.Type.AUTH) {
	Auth auth = (Auth) request.payload;
	// Agora temos acesso a auth.user e auth.password;
} // ...
```


Para escrever os dados nos ficheiros, devemos ter uma classe responsável por receber um objecto qualquer e escrevê-lo no disco.
```java
class Data {
	public void write(Object object);
	public Object read();
}
```

A responsabilidade de utilizar esta classe é do servidor, ou seja, o servidor tem uma estrutura de dados qualquer e apenas pede à Data para guardar essa estrutura em disco. A estrutura em si podem ser as classes User e Wine (Image já faz parte do Java).

Por exemplo, no servidor:
```java
Hashtable<String, User> users;

// Na inicialização do servidor:
users = (Hashtable<String, User>) db.read();

// Sempre que alterarmos os dados (no fim do ciclo de atendimento a pedidos):
db.write(users); // <- Todos os dados estão dentro do 'users'
```
