function min_dist = cal_min_dist(idx,X)
%X is the data matrix where the rows are points and columns are variables 
% idx is the index of data points after clustering
min_dist = 0; %initialize the value of minimun distance
for i=1:max(idx)
    
    cluster = find(idx==i);
    
    if size(cluster,1)~=1 % if there are more than one elment in a cluster
        
        for j=1:size(cluster,1)-1
            
            pt1 = X(j,:);
            pt2 = X(j+1,:);
            
            dist = norm(pt2-pt1);  
            
            if(dist<min_dist) 
                min_dist = dist ;%select the mininum distance
            end
            
        end
        
        
    end
    
    
end

