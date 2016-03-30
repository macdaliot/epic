testVecs = importdata('testVecs.mat');

testVecs = testVecs([5001:size(testVecs,1)],:);

%%
size(testVecs)
%size(words)
dims = tsne(testVecs, [], 2, size(testVecs,2));
%%
dlmwrite('vectors.txt',dims,'delimiter',' ','-append');
%%
dims = importdata('vectors.txt')
%% 3D
clf
plot3(dims(:,1),dims(:,2), dims(:,3),'.')
hold on
for i = 1: length(dims(:,1))
    text(dims(i,1),dims(i,2),dims(i,3),wordsStruct(i,:))
end

%% 2D
clf
plot(dims(:,1),dims(:,2),'*g','MarkerSize',1)
hold on
for i = 1:length(wordsStruct)
    if(strcmp(cellstr(wordsStruct(i,:)),char('malware')))
        mal = i;
        plot(dims(mal,1),dims(mal,2),'b*')
        text(dims(mal,1),dims(mal,2),wordsStruct(mal,:),'Color','Blue','FontSize',14)
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('aegis')))
        cat = i;
        plot(dims(cat,1),dims(cat,2),'b*')
        text(dims(cat,1),dims(cat,2),wordsStruct(cat,:),'Color','Blue','FontSize',14)
        
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('protect')))
        kitten = i;
        plot(dims(kitten,1),dims(kitten,2),'b*')
        text(dims(kitten,1),dims(kitten,2),wordsStruct(kitten,:),'Color','Blue','FontSize',14)
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('dog')))
        cat = i;
        plot(dims(cat,1),dims(cat,2),'b*')
        text(dims(cat,1),dims(cat,2),wordsStruct(cat,:),'Color','Blue','FontSize',14)
        
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('cat')))
        kitten = i;
        plot(dims(kitten,1),dims(kitten,2),'b*')
        text(dims(kitten,1),dims(kitten,2),wordsStruct(kitten,:),'Color','Blue','FontSize',14)
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('kitten')))
        cat = i;
        plot(dims(cat,1),dims(cat,2),'b*')
        text(dims(cat,1),dims(cat,2),wordsStruct(cat,:),'Color','Blue','FontSize',14)
        
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('robot')))
        kitten = i;
        plot(dims(kitten,1),dims(kitten,2),'b*')
        text(dims(kitten,1),dims(kitten,2),wordsStruct(kitten,:),'Color','Blue','FontSize',14)
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('encryption')))
        cat = i;
        plot(dims(cat,1),dims(cat,2),'b*')
        text(dims(cat,1),dims(cat,2),wordsStruct(cat,:),'Color','Blue','FontSize',14)
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('hacking')))
        kitten = i;
        plot(dims(kitten,1),dims(kitten,2),'b*')
        text(dims(kitten,1),dims(kitten,2),wordsStruct(kitten,:),'Color','Blue','FontSize',14)
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('bug')))
        cat = i;
        plot(dims(cat,1),dims(cat,2),'b*')
        text(dims(cat,1),dims(cat,2),wordsStruct(cat,:),'Color','Blue','FontSize',14)
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('attack')))
        kitten = i;
        plot(dims(kitten,1),dims(kitten,2),'b*')
        text(dims(kitten,1),dims(kitten,2),wordsStruct(kitten,:),'Color','Blue','FontSize',14)
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('criminals')))
        cat = i;
        plot(dims(cat,1),dims(cat,2),'b*')
        text(dims(cat,1),dims(cat,2),wordsStruct(cat,:),'Color','Blue','FontSize',14)
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('malicous')))
        kitten = i;
        plot(dims(kitten,1),dims(kitten,2),'b*')
        text(dims(kitten,1),dims(kitten,2),wordsStruct(kitten,:),'Color','Blue','FontSize',14)
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('yoga')))
        cat = i;
        plot(dims(cat,1),dims(cat,2),'b*')
        text(dims(cat,1),dims(cat,2),wordsStruct(cat,:),'Color','Blue','FontSize',14)
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('danger')))
        kitten = i;
        plot(dims(kitten,1),dims(kitten,2),'b*')
        text(dims(kitten,1),dims(kitten,2),wordsStruct(kitten,:),'Color','Blue','FontSize',14)
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('dangerous')))
        cat = i;
        plot(dims(cat,1),dims(cat,2),'b*')
        text(dims(cat,1),dims(cat,2),wordsStruct(cat,:),'Color','Blue','FontSize',14)
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('intruders')))
        kitten = i;
        plot(dims(kitten,1),dims(kitten,2),'b*')
        text(dims(kitten,1),dims(kitten,2),wordsStruct(kitten,:),'Color','Blue','FontSize',14)
    end
    if(strcmp(cellstr(wordsStruct(i,:)),char('are')))
        cat = i;
        plot(dims(cat,1),dims(cat,2),'b*')
        text(dims(cat,1),dims(cat,2),wordsStruct(cat,:),'Color','Blue','FontSize',14)
    end
    %text(dims(i,1),dims(i,2),wordsStruct(i,:))
    
end


