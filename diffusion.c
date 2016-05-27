#include <stdio.h>
#include <uuid/uuid.h>
#include <string.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>


char* randomId(){
	uuid_t uuid;
	uuid_generate_time_safe(uuid);
	char* id = malloc(sizeof(char)*9);
	strncpy(id, uuid, 8);
	id[9]='\0';
	return id;
}

int random_8(int a, int b){
    return rand()%(b-a) +a;
}

char* formatInt(int i, int max){
	char* res = malloc(sizeof(char)*(max+1));
	sprintf(res, "%d", i);
	while(strlen(res) < max){
		res = strcat("0",res);
	}
	return res;
}

int main() {
	printf("entity_address entity_TCP_port message_to_be_transferred\n");
	char response[600];
	scanf("%s", response);
	char entity_address[16];
	char entity_TCP_port[5];
	char* message_to_be_transferred = malloc(sizeof(char)*486);

	const char s[2] = "#";
	char *token;
   	/* get the first token */
	token = strtok(response, s);

	strcpy(entity_address, token);
		printf( " %s\n", token );
  	/* walk through other tokens */
	int i = 1;
	int b = 1;
	while (b) {
		token = strtok(NULL, s);
		if(i==1){
			strcpy(entity_TCP_port, token);
			i++;
		}
		else if(i==2){
			strcpy(message_to_be_transferred, token);
			i++;
			b = 0;
		}
		printf( " %s\n", token );
	}
	printf("probleme idm");
	char* idm = randomId();
	/*int idm_i = random_8(10000000,99999999);
	char* idm = malloc(sizeof(char)*9);
	sprintf(idm, "%d", idm_i);*/
	printf("probleme formatInt");
	char* len_msg = formatInt(strlen(message_to_be_transferred),3);
	char* res = strcat( strcat(strcat("APPL ", idm), strcat(strcat("DIFF#### ", len_msg) , " ") ), message_to_be_transferred );

	int sock = socket(PF_INET,SOCK_STREAM,0);
	struct sockaddr_in adress_sock;
	adress_sock.sin_family = AF_INET;
	adress_sock.sin_port = htons(atoi(entity_TCP_port));
	inet_aton(entity_address,&adress_sock.sin_addr);
	int descr=socket(PF_INET,SOCK_STREAM,0);
	int r=connect(descr,(struct sockaddr *)&adress_sock,
	sizeof(struct sockaddr_in));
	if(r!=-1){
		write(sock,res,strlen(res)*sizeof(char));
	} else {
		printf("Probleme de connexion!\n");
	}
	close(sock);

	return 0;
}