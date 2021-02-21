function indices = solr_distance_grouping_fun()
%some constants
%use the speed of sound at the temperature and air pressure 
%of the space where the data collection was conducted
speedofsound = 34300; % cm/s  % change according to the temperature and air pressure
samplingrate = 44100;  % sampling rate of the audio files; change it according to the audio files used
fpass = 17000;

%%%following parameters need to be set properly
n = 1; % 1: ultrasonic 1s; 2: ultrasonic 0.1s, 3: ultrasonic 0.5s
       % 4: full range 1s; 5: full range 0.1s 6: full range 0.5s
       
rootFolder = "./data/clustering_data";

% change the below data folders accordingly
% now it has three folders representing three sensor units' data
% place the probing files received by each sensor unit in a folder
% create more folders if there are more than three sensor units

device1_folder = './data/clustering_data/device1'; 
device2_folder = './data/clustering_data/device2';  
device3_folder = './data/clustering_data/device3'; 
device4_folder = './data/clustering_data/device4'; 


%dataFolders = {device1_folder,device2_folder};
dataFolders = {device1_folder,device2_folder,device3_folder,device4_folder};



% number of clusters (closed and open spaces)
numClusters =2;
db=500; %this value needs to be determined through experiments

switch n
    case 1
        % ultrasound 1s
        [s2,fs2] = audioread('./data/probefiles/sweep17000hz20000hz3dbfs1s.wav');
        duration = 1;
    case 2
         % ultrasound 0.1s
        [s2,fs2] = audioread('./data/probefiles/sweep17000hz20000hz3dbfsdot1s.wav');
        duration = 0.1;
    case 3
         % ultrasound 0.5s
        [s2,fs2] = audioread('./data/probefiles/sweep17000hz20000hz3dbfsdot5s.wav');
        duration = 0.5;
    case 4
        % full range 1s
        [s2,fs2] = audioread('./data/probefiles/sweep20hz20000hz3dbfs1s.wav');
        duration = 1;
    case 5
        [s2,fs2] = audioread('./data/probefiles/sweep20hz20000hz3dbfsdot1s.wav');
        duration = 0.1;
    case 6
        [s2,fs2] = audioread('./data/probefiles/sweep20hz20000hz3dbfsdot5s.wav');
        duration = 0.5;
    otherwise
        disp('input value is wrong')
end

targetsignal = (s2 - mean(s2)) / std(s2);

NumFiles = 0;
for idx = 1: numel(dataFolders)
    dataFolder = dataFolders{idx};
    %display(dataFolder);
    outputfile = 'result.csv';
    fullname = fullfile(dataFolder, outputfile);
    fid=fopen(fullname,'w');
    filePattern = fullfile(dataFolder, '*.wav'); 
    FileList = dir(filePattern);
    N = size(FileList,1);
    NumFiles = N;
    for k = 1:N
        % get the file name:
        filename = FileList(k).name;
      %  disp(filename);

        % process
        fullname = fullfile(dataFolder,filename);
        [s0,fs0] = audioread(fullname);
        s1 = highpass(s0,fpass, fs0);
        signal1 = (s1 - mean(s1))/std(s1); 

        % search to find the target signal 
        lag = finddelay(targetsignal,signal1(1: length(signal1)));
        
      

        fs1 = fs0; %retain the same sampling rate?
        
         
          
        
        signalStrength = 0;
        
         if (lag + round(fs1 * duration)>=length(signal1)||lag<0)
               % lag = round(length(signal1)/2);
                signalStrength = 0;
        
          
         else  
             
              for j = lag : lag + round(fs1 * duration)
            
                 if floor(j)~=j || j<=0
                     j %for some reason negative interger and non-integer has been detected
                 end
               
                 if j>size(s1,1)
                     j
                 end
            
                 signalStrength = signalStrength + abs(s1(j));
              end
         end
        
         
        fprintf(fid, '%d, %f\n', lag, signalStrength);
    end
    fclose(fid);
end

signalStrengthsFilename = fullfile(rootFolder,"SignalStrengths.csv");
fid = fopen(signalStrengthsFilename,'w');
results = zeros(numel(dataFolders),NumFiles);
for idx = 1: numel(dataFolders)
    dataFolder = dataFolders{idx};
    inputfile = 'result.csv';
    fullname = fullfile(dataFolder, inputfile);
    data = csvread(fullname);
    fprintf(fid, '%d,', idx);
    for i = 1: length(data)
        results(idx,i) = data(i,2);
        fprintf(fid, '%f,', results(idx,i));
    end
    fprintf(fid, '\n');
end
fclose(fid);

%disp(results);
results_modified = modify_results(results);

%rank the results first
[results_sorted,order_sorted] = sort_results(results_modified);
%

%[idx,C,sumd] = kmeans(results_modified, 2,'Distance','cityblock','Replicates',1);%make sure the diagonal results are the same


[idx,C,sumd] = kmeans(results_sorted, numClusters,'Replicates',20);%make sure the diagonal results are the same



%[idx,C] = kmeans(results,  clustering(results,db));%by default the number of cluster is 1
%disp(idx)

%resultfilename = fullfile(rootFolder,"ClusterResults.csv");
%fid = fopen(resultfilename,'w');

%data = csvread(signalStrengthsFilename);

%for i = 1: size(data,1)
 %   fprintf(fid, '%d,%d\n', idx(i), data(i,1));
%end
%fclose(fid);

%disp('finished.');



indices = cat(2,idx',order_sorted);
end


