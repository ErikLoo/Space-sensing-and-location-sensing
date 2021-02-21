function results_modified = modify_results(results)
%results is a nxn matrix where the 

results_modified = results; 
for i=1:size(results,2)
    results_modified(i,i)=50000;
end


end
