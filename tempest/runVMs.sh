#!/bin/bash


for i in  {1..7}
do
   scp target/uber-tempest-1.0-SNAPSHOT.jar lekkala2@fa15-cs425-g03-0$i.cs.illinois.edu:~/
done

ssh vishnukonda@10.0.0.15  mkdir -p .ssh
cat .ssh/id_rsa.pub | ssh vishnukonda@10.0.0.15 'cat >> .ssh/authorized_keys'

scp /Users/swapnalekkala/swapna/res2.tex vishnukonda@10.0.0.15:/Users/vishnukonda/swapna
ssh vishnukonda@10.0.0.15
cd /Users/vishnukonda/swapna
pdflatex res2.tex 
scp /Users/vishnukonda/swapna/res2.pdf swapnalekkala@10.0.0.18:/Users/swapnalekkala/swapna/res2.pdf
