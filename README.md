Simple Arango db access

Input json for the execution.
<p>[header_list.csv](..%2FDownloads%2Fheader_list.csv)
    {
    "url" : "http://localhost:8529/_db/key2publish/_api",
    "userName": "root",
    "password" : "key2publish",
    "file" : "C:\\CPK2P\\CSV-Import\\stjhon.csv",
    "collection" : "k2p_genome",
    "uniqueKey" : "ITK artikel nummer",
    "batchSize" : 3000,
    "action" : "import",
    "columnSeparator" : "\t",
    "encoding" : "UTF-16"
    }
</p>

