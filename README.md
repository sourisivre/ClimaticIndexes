# ClimaticIndexes
Download climatic indexes values from NOAA and perform monthly correlation between them and flow data.

Note: You will need [Jsoup library](https://jsoup.org/download).

## Fluxogram
1. You have a list of [Climatic Indexes](https://github.com/sourisivre/ClimaticIndexes/blob/main/files/iURL.txt) here called `iURL.txt` In this file there are abreviations and online repository path of those indexes
2. You download them and check for inconsistencies, such as zeros and null values
3. You also have a historical serie of flows from a fluviometrical station `input.txt`
4. Now you can perform a correlation between Climatic Indexes and flow values

## Correlation aproaches

I programmed aproaches for the correlation computed to each quarter of the year, for each semester and for the entire year with lag0 and lag1.

## Further information

Please check `draft.pdf`, which condenses some of the main results and a little more of what was developed in this course. 

_Note that this pdf file is not the final report._

<sub>This code was developed in order to provide data for a PhD course final work around 2020.</sub>
