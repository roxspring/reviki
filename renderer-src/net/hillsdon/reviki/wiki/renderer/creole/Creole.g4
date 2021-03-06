/* Todo:
 *  - Allow arbitrarily-nested lists (see actions/attributes)
 */

parser grammar Creole;

options {
  // Where to find the token definitions
  tokenVocab=CreoleTokens;

  // Use a custom superclass, rather than the stock ANTLR parser
  superClass=ContextSensitiveParser;
}

/* ***** Top level elements ***** */

// A page consists of a sequence of block elements, separated by newlines.
creole    : (block (LineBreak | ParBreak)*)* ;

block     : heading
          | ulist | olist
          | hrule
          | table
          | code | nowiki
          | terseblockquote
          | blockquote
          | directive
          | paragraph
          ;

/* ***** Block Elements ***** */

heading   : HSt WS? {disallowBreaks();} inline {unsetBreaks();} HEnd;

paragraph : inline ;

// This is horrible. Sadly, I couldn't figure out how to do arbitrarily-nested
// lists, so up to 10 levels of nesting are hardcoded here. There's a bit of
// noise due to linebreaks, but it's not so complicated. A list consists of a
// sequence of list items, each of which has some content (`inList`), and some
// potential child list elements. All but the last child element can include
// linebreaks. The last child cannot, because then detecting the end of a list
// is ambiguous.
// If you want/need a linebreak in the last child, you can use an inline break.
ulist      : {allowBreaks();} (ulist1 LineBreak?)* {unsetBreaks();} {disallowBreaks();} ulist1 LineBreak? {unsetBreaks();};
ulist1     : U1  inList LineBreak? ({allowBreaks();} list2* {unsetBreaks();} {disallowBreaks();} list2 {unsetBreaks();})? ;
ulist2     : U2  inList LineBreak? ({allowBreaks();} list3* {unsetBreaks();} {disallowBreaks();} list3 {unsetBreaks();})? ;
ulist3     : U3  inList LineBreak? ({allowBreaks();} list4* {unsetBreaks();} {disallowBreaks();} list4 {unsetBreaks();})? ;
ulist4     : U4  inList LineBreak? ({allowBreaks();} list5* {unsetBreaks();} {disallowBreaks();} list5 {unsetBreaks();})? ;
ulist5     : U5  inList LineBreak? ({allowBreaks();} list6* {unsetBreaks();} {disallowBreaks();} list6 {unsetBreaks();})? ;
ulist6     : U6  inList LineBreak? ({allowBreaks();} list7* {unsetBreaks();} {disallowBreaks();} list7 {unsetBreaks();})? ;
ulist7     : U7  inList LineBreak? ({allowBreaks();} list8* {unsetBreaks();} {disallowBreaks();} list8 {unsetBreaks();})? ;
ulist8     : U8  inList LineBreak? ({allowBreaks();} list9* {unsetBreaks();} {disallowBreaks();} list9 {unsetBreaks();})? ;
ulist9     : U9  inList LineBreak? ({allowBreaks();} list10* {unsetBreaks();} {disallowBreaks();} list10 {unsetBreaks();})? ;
ulist10    : U10 inList ;

olist      : {allowBreaks();} (olist1 LineBreak?)* {unsetBreaks();} {disallowBreaks();} olist1 LineBreak? {unsetBreaks();};
olist1     : O1  inList LineBreak? ({allowBreaks();} list2* {unsetBreaks();} {disallowBreaks();} list2 {unsetBreaks();})? ;
olist2     : O2  inList LineBreak? ({allowBreaks();} list3* {unsetBreaks();} {disallowBreaks();} list3 {unsetBreaks();})? ;
olist3     : O3  inList LineBreak? ({allowBreaks();} list4* {unsetBreaks();} {disallowBreaks();} list4 {unsetBreaks();})? ;
olist4     : O4  inList LineBreak? ({allowBreaks();} list5* {unsetBreaks();} {disallowBreaks();} list5 {unsetBreaks();})? ;
olist5     : O5  inList LineBreak? ({allowBreaks();} list6* {unsetBreaks();} {disallowBreaks();} list6 {unsetBreaks();})? ;
olist6     : O6  inList LineBreak? ({allowBreaks();} list7* {unsetBreaks();} {disallowBreaks();} list7 {unsetBreaks();})? ;
olist7     : O7  inList LineBreak? ({allowBreaks();} list8* {unsetBreaks();} {disallowBreaks();} list8 {unsetBreaks();})? ;
olist8     : O8  inList LineBreak? ({allowBreaks();} list9* {unsetBreaks();} {disallowBreaks();} list9 {unsetBreaks();})? ;
olist9     : O9  inList LineBreak? ({allowBreaks();} list10* {unsetBreaks();} {disallowBreaks();} list10 {unsetBreaks();})? ;
olist10    : O10 inList ;

list2      : (olist2 | ulist2) LineBreak? ;
list3      : (olist3 | ulist3) LineBreak? ;
list4      : (olist4 | ulist4) LineBreak? ;
list5      : (olist5 | ulist5) LineBreak? ;
list6      : (olist6 | ulist6) LineBreak? ;
list7      : (olist7 | ulist7) LineBreak? ;
list8      : (olist8 | ulist8) LineBreak? ;
list9      : (olist9 | ulist9) LineBreak? ;
list10     : (olist10 | ulist10) LineBreak? ;

inList     : (WS? listBlock ({canBreak()}? LineBreak)?)+ ;

listBlock  : code | nowiki | inline ;

hrule      : Rule ;

table      : (trow (RowEnd | LineBreak))* trow (RowEnd | LineBreak)? ;
trow       : tcell+ ;
tcell      : th | td ;
th         : ThStart inline? ;
td         : TdStart inTable* ;

inTable    : {disallowBreaks();} (ulist | olist | code | nowiki | inline) {unsetBreaks();} ;

nowiki     : NoWiki NoWikiAny EndNoWiki ;

terseblockquote : TerseBlockquoteSt creole TerseBlockquoteEnd ;

blockquote : BlockquoteSt creole BlockquoteEnd ;

directive  : DirectiveEnable MacroName (MacroSep MacroEnd | MacroEndNoArgs) # Enable
           | DirectiveDisable MacroName (MacroSep MacroEnd | MacroEndNoArgs) # Disable
           ;

/* ***** Inline Elements ***** */

inline     : inlinestep+ ;

inlinestep : bold | italic | sthrough
           | link | titlelink | simpleimg | imglink | wikiwlink | attachment | rawlink
           | jiralink | jiratitlelink
           | anchor
           | inlinecode | preformat
           | linebreak
           | macro
           | any
           ;

bold       : BSt inline? BEnd ;

italic     : ISt inline? IEnd ;

sthrough   : SSt inline? SEnd ;

link       : LiSt InLink LiEnd ;

jiralink   : JIRALiSt JIRAInLink? JIRALiEnd;

titlelink  : LiSt InLink? Sep InLinkEnd LiEnd2 ;

jiratitlelink : JIRALiSt JIRAInLink? JIRASep JIRAInLinkEnd? JIRALiEnd2;

imglink    : ImSt InLink? Sep InLinkEnd ImEnd2 ;

simpleimg  : ImSt InLink ImEnd ;

wikiwlink  : WikiWords ;

attachment : Attachment ;

rawlink    : RawUrl ;

anchor     : AnSt InAnchor AnEnd ;

preformat  : NoWiki NoWikiInlineAny EndNoWiki ;

linebreak  : ({canBreak()}? LineBreak)? InlineBrk LineBreak? ;

macro      : MacroSt MacroName (MacroSep MacroEnd | MacroEndNoArgs) ;

any        : Any | WS | {canBreak()}? LineBreak ;

/* ***** Syntax Highlighting ***** */

code        : codetag | html | codeblock ;
inlinecode  : inlinecodetag | inlinehtml | inlinecodeblock;

codeblock      : CodeStart CodeAny CodeEnd ;
inlinecodeblock : CodeInlineStart ;
codetag        : CodeTagStart CodeTagAny CodeTagEnd ;
inlinecodetag  : CodeTagStart CodeTagInlineAny CodeTagEnd ;

html        : HtmlStart HtmlAny CodeTagEnd ;
inlinehtml  : HtmlStart HtmlInlineAny CodeTagEnd ;
