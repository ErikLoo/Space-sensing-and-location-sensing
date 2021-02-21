function results_modified2 = modify_results2(results)
%results is a nxn matrix where the 

results_modified2 = (sort(results','descend'))';

for i=1:size(results,2)
   
    results_modified2(i,1) = 0;
end


end
