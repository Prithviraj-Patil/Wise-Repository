#!/usr/bin/perl

open(PARSE_FD,"< t.txt") || die "can't open t.txt";
while(<PARSE_FD>){
  #chop;
  my @fl = split/'\t'/,$_;
  @foo  = grep(/:*/,$_);
  print $foo[0];
  print $fl[0];
}
