#include <stdio.h>
#include <uuid/uuid.h>
#include <string.h>

char* randomId(){
	uuid_t uuid;
	uuid_generate_time_safe(uuid);
	char id[9];
	strncpy(id, uuid, 8);
	id[9]='\0';
}

char* formatInt(int i, int max){
	char res[15];
	sprintf(res, "%d", i);
	while(strlen(res) < max){
		res = "0"+res;
	}
}

int main() {
	printf("entity_address entity_TCP_port message_to_be_transferred");
	char response[255];
	scanf("%s", response);
	char entity_address[255];
	char entity_TCP_port[255];
	char message_to_be_transferred[255];

	const char s[2] = " ";
	char *token;
   	/* get the first token */
	token = strtok(response, s);

	strcpy(entity_address, token);
  	/* walk through other tokens */
	int i=1;
	while( token != NULL ) 
	{
		printf( " %s\n", token );
		token = strtok(NULL, s);
		if(i==1){
			strcpy(entity_TCP_port, token);
			i++;
		}
		else if(i==2){
			strcpy(message_to_be_transferred, token);
			i++:
		}
	}
	char idm[9] = randomId();
	char mess[1024];
	snprintf(mess, sizeof(mess), "APPL %C DIFF### %C %C", idm, formatInt(strlen(entity_TCP_port), 2), mess);

	int sock=socket(PF_INET,SOCK_STREAM,0);
	struct sockaddr_in *addressin;
	struct addrinfo *first_info;
	struct addrinfo hints;
	bzero(&hints,sizeof(struct addrinfo));
	hints.ai_family = AF_INET;
	hints.ai_socktype=SOCK_STREAM;
	int r=getaddrinfo(entity_address,entity_TCP_port,&hints,&first_info);
	if(r==0){
		if(first_info!=NULL){
			addressin=(struct sockaddr_in *)first_info->ai_addr;
			int ret=connect(sock,(struct sockaddr *)addressin,(socklen_t)sizeof(struct
				sockaddr_in));
			if(ret==0){
				write(sock,mess,strlen(mess)*sizeof(char));
			}
			else{
				printf("Probleme de connexion!\n");
			}
			close(sock);
		}
	}

	return 0;
}