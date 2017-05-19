# WebScraper
Simple command line web scraper
- accepts as command line parameters:

o web resources URL or path to plain text file containing a list of URLs where each URL is contained on a different line

o data command(s)

o word (or list of words with “,” delimiter)

o output verbosity flag, if on then the output contains information about time spent on data scraping and data processing (-v)

- supports the following data processing commands:

o count number of provided word(s) occurrence on webpage(s). (-w)

o count number of characters of each webpage (-c)

o extract sentences which contain given words (-e)


Data processing results are printed to output for each web resources separately and for all resources as total.
Command line parameters example:
java –jar WebScraper.jar /Users/userName/Folder/links.txt Greece,default –v –w –c –e
