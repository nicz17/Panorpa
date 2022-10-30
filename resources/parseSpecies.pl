#!/usr/bin/perl

sub  trim { my $s = shift; $s =~ s/^\s+|\s+$//g; return $s };

sub readFile {
    open CAP, "liste-especes.data";
    my $nLines = 0;
    while (<CAP>) {
	$nLines++;
	chomp;
	my @cols = split("\t");
	$french = trim $cols[0];
	$latin = trim $cols[1];
        $family = trim $cols[2];
	#print "  <$latin> <$french> <$family>\n";
	$latin =~ s/ /\\ /g;

	#print "$latin=$family\n";
	print "$latin=$french\n";
    }
    close CAP;
    #print "Parsed $nLines lines\n";
}


readFile;

