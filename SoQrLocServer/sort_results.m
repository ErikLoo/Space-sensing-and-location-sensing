function [results_sorted,order_sorted] = sort_results(results)
%results is a nxn matrix where the 

order = 1:size(results,1); 

for i=1:size(results,1)-1
   for j=1:size(results,1)-1
       if(results(j,1)<results(j+1,1))
           %switch the two items
           temp1 = results(j,:);
           results(j,:) = results(j+1,:);
           results(j+1,:) = temp1;
           
           temp2 = order(j);
           order(j) = order(j+1);
           order(j+1) = temp2; 
       end
   end
end

results_sorted = results; 
order_sorted = order; 
end
