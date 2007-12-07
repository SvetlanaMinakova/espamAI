%parameter NumFrames 1 100;
%parameter VNumBlocks 2 100;
%parameter HNumBlocks 1 100;

%typedef HeaderInfo             THeaderInfo;
%typedef LuminanceQTable        TQTables;
%typedef ChrominanceQTable      TQTables;
%typedef LuminanceHuffTableDC   THuffTablesDC;
%typedef ChrominanceHuffTableDC THuffTablesDC;
%typedef LuminanceHuffTableAC   THuffTablesAC;
%typedef ChrominanceHuffTableAC THuffTablesAC;
%typedef LuminanceTablesInfo    TTablesInfo;
%typedef ChrominanceTablesInfo  TTablesInfo;
%typedef Packets                TPackets;
%typedef Block                  TBlocks;

for k = 1:1:1,
  [  LuminanceQTable,     ChrominanceQTable,
     LuminanceHuffTableDC,ChrominanceHuffTableDC,
     LuminanceHuffTableAC,ChrominanceHuffTableAC,
     LuminanceTablesInfo, ChrominanceTablesInfo
  ] = DefaultTables();
end


for k = 1:1:NumFrames,

  [  HeaderInfo  ] = VideoInInit();

  for j = 1:1:VNumBlocks,
    for i = 1:1:HNumBlocks,

      [ Block ] = VideoInMain();

      [ Block ] = DCT( Block );

      [ Block ] = Q( Block, LuminanceQTable, ChrominanceQTable );

      [ Packets ] = VLE( Block,
                         LuminanceHuffTableDC,ChrominanceHuffTableDC,
                         LuminanceHuffTableAC,ChrominanceHuffTableAC );


      [  dummy  ] = VideoOut( HeaderInfo, LuminanceTablesInfo,
                              ChrominanceTablesInfo, Packets );

    end
  end


end
