function number_of_clusters = clustering(X,db)
%this method returns the  number of clusters
%where X is the data matrix
%db is the threshold for classifying the clusters
%the defaults number of clusters is 0

clusters = {}; 
number_of_clusters = 1; 

%make the clusters
for i=1:size(X,1)
    col = X(:,i);
    cluster = find(col>db); 
    clusters = cat(1,clusters,cluster);  
end

%filter out identical clusters
for j=1:size(clusters,1)-1
    row1 = clusters(j,:);
    for k=j+1:size(clusters,1)
        row2 = clusters(k,:); 
        
        if isequal(row1,row2)==1
            break; 
        end
        
        if k==size(clusters,1)
            number_of_clusters = number_of_clusters+1; 
        end
        
    end
    
end




end

