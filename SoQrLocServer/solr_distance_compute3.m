function my_distance = solr_distance_compute3()
%%%algorithm to compute the distance between two sensor units

%some constants
%use the speed of sound at the temperature and air pressure 
%of the space where the data collection was conducted
speedofsound = 34300; % cm/s 
samplingrate = 44100;  % sampling rate of the audio files; change it accordingly
% if the sensors are placed in parallel, then adjustment is needed
device1Speak2Mic = 0; % distance between the speaker and microphone of the sensor device 1 (unit: cm)
device2Speak2Mic = 0; % distance between the speaker and microphone of the sensor device 2 (unit: cm)


% probe audio file used in this experiment
% use the correct probe sound for the collected data files

% specify the probe audio file as follows

% probe soundfor the old data
%[s2,fs2] = audioread('./data/probefiles/sin20hz20000hz.wav'); 

% full range 1s
%[s2,fs2] = audioread('./data/probefiles/sweep20hz20000hz3dbfs1s.wav');


% full range 0.1s
%[s2,fs2] = audioread('./data/probefiles/sweep20hz20000hz3dbfsdot1s.wav');

%fullrange 0.5s
%[s2,fs2] = audioread('./data/probefiles/sweep20hz20000hz3dbfsdot5s.wav');

%ultrasound 1s
%[s2,fs2] = audioread('./data/probefiles/sweep17000hz20000hz3dbfs1s.wav');

%ultrasound 0.1s
%[s2,fs2] = audioread('./data/probefiles/sweep17000hz20000hz3dbfsdot1s.wav');

%ultrasound 0.5s
% [s2,fs2] = audioread('./data/probefiles/sweep17000hz20000hz3dbfsdot5s.wav');

%ultrasound 0.2s
[s2,fs2] = audioread('./data/probefiles/sweep17000hz22000hz17000hzdot1s.wav');


targetsignal = (s2 - mean(s2)) / std(s2);

% place the probe files from one device into this folder
%dataFolder1 = './data/experimentdata/olddata/device1';

%dataFolder1 = './data/experimentdata/June22/Device1FullRange/1s';

%dataFolder1 = './data/experimentdata/June22/Device1FullRange/dot1';

%dataFolder1 = './data/experimentdata/June22/Device1FullRange/dot5';

%dataFolder1 = './data/experimentdata/June22/Device1UltrasonicRange/1s';

%dataFolder1 = './data/experimentdata/June22/Device1UltrasonicRange/dot1';

%dataFolder1 = './data/experimentdata/June22/Device1UltrasonicRange/dot5';

%dataFolder1 = './data/experimentdata/June26/Device 1 full range no sensor unit/1s';
dataFolder1 = './data/experimentdata/dataCollectionClosedEnvironment/US/100%/0.2/No_degree/40/device1';

outputfile1 = 'device1.csv';
fullname1 = fullfile(dataFolder1, outputfile1);
fid=fopen(fullname1,'w');
filePattern = fullfile(dataFolder1, '*.wav'); 
FileList1 = dir(filePattern);
N = size(FileList1,1);

disp('file names from the first folder')
for k = 1:N
    % get the file name:
    filename = FileList1(k).name;
    disp(filename);
    
    % process
    fullname = fullfile(dataFolder1,filename);
    [s1,fs1] = audioread(fullname);
    signal1 = (s1 - mean(s1))/std(s1);
    
    % search the first half to find the target signal 
    lag = finddelay(targetsignal,signal1(1: length(signal1)/2));
    
    lag2 = finddelay(targetsignal,signal1(length(signal1)/2+1: length(signal1)));
    lag2 = lag2 + length(signal1)/2;
    fprintf(fid, '%d,%d \n', [lag,lag2]);
end
fclose(fid);


%place the probe files from the second device into this folder
%dataFolder2 = './data/experimentdata/olddata/device2';

%dataFolder2 = './data/experimentdata/June22/Device2FullRange/1s';

%dataFolder2 = './data/experimentdata/June26/Device 2 full range no sensor unit/1s';

%dataFolder2 = './data/experimentdata/June22/Device2FullRange/dot5';

%dataFolder2 = './data/experimentdata/June22/Device1UltrasonicRange/1s';

%dataFolder2 = './data/experimentdata/June22/Device1UltrasonicRange/dot1';

dataFolder2 = './data/experimentdata/dataCollectionClosedEnvironment/US/100%/0.2/No_degree/40/device2';


outputfile2 = 'device2.csv';
fullname2 = fullfile(dataFolder2, outputfile2);
fid=fopen(fullname2,'w');
filePattern = fullfile(dataFolder2, '*.wav'); 
FileList2 = dir(filePattern);

% the number of files should be exactly the same as the previous one
N2 = size(FileList2,1);

if N == N2
    disp('file names from the second folder')
    for k = 1:N2    
        % get the file name:
        filename = FileList2(k).name;
        disp(filename);

        C = textscan(filename, '%s', 'delimiter', '_');    
        C = C{1};
        class = str2double(char(C(1))); 
        
        % process
        fullname = fullfile(dataFolder2,filename);
        [s1,fs1] = audioread(fullname);
        signal1 = (s1 - mean(s1))/std(s1);
        
        lag = finddelay(targetsignal,signal1(1:length(signal1)/2));

        lag2= finddelay(targetsignal,signal1(length(signal1)/2+1 : length(signal1)));
        lag2 = lag2 + length(signal1)/2;
        
        fprintf(fid, '%d,%d,%d\n', [lag,lag2,class]);
    end
    fclose(fid);

    %compute distance between the four timestampes fromt the two files and
    %save the result to file
    %for old data
    %resultfilename = './data/experimentdata/olddata/result_fullrange.csv';
    
    %resultfilename = './data/experimentdata/June22/result_fullrange_1s.csv';
    
    %resultfilename = './data/experimentdata/June22/result_fullrange_dot1s.csv';
    
    %resultfilename = './data/experimentdata/June22/result_fullrange_dot5s.csv';
    
    %resultfilename = './data/experimentdata/June22/result_ultrasoundrange_1s.csv';
    
    %resultfilename = './data/experimentdata/June22/result_ultrasoundrange_dot1s.csv';
    
    resultfilename = './data/experimentdata/dataCollectionClosedEnvironment/US/100%/0.2/No_degree/40/result_fullrange_dot1s.csv';
    
    fid = fopen(resultfilename,'w');
    fprintf(fid, 'index,groundtruth,distance,distanceAdjust\r\n');
    data1 = csvread(fullname1);
    data2 = csvread(fullname2);
    for i = 1: length(data1)
       distance = abs(data1(i,2) - data1(i,1) - (data2(i,2) - data2(i,1))) * speedofsound / (samplingrate *2);  
       distanceAdjust = distance + device1Speak2Mic + device2Speak2Mic;
       fprintf(fid, '%d,%d,%f,%f\r\n', [i,data2(i,3),distance,distanceAdjust]);%write data to files
    end
    fclose(fid);
    disp('finished.');
else
    disp('the number of files in the two data folders should be the same.');
end

my_distance = distance; %output the distance; 