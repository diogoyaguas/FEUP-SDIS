echo "Creating peers"
cd bin 
gnome-terminal -- java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=mortelenta program.PeerInit 8080   
for (( c=1; c<2; c++ ))
do  
	sleep 1s
	gnome-terminal -- java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=mortelenta program.PeerInit 808$c localhost 8080
done

