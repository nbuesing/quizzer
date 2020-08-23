function version(r) {
  r.return(200, njs.version);
}

function hello(r) {
  r.return(200, "Hello world!\n" + partition("foo", 6));
}


/**
 * Always returns a consistent positiv number
 * Used by the Java client's partitioning function https://github.com/apache/kafka/blob/1.0/clients/src/main/java/org/apache/kafka/common/utils/Utils.java#L741
 * @param {Number} n get's bit-wise multiplicated with 0x7fffffff
 * @returns {Number} which is n bit-wise multiplocated with 0x7fffffff
 */
function _toPositive(n) {
  return n & 0x7fffffff
}

/**
 * Returns the partition number according to the Kafka Java client's default partitioning function, based on a key and the number of partitions
 * @param {String} key used by the partitioning function
 * @param {Number} partitionCount number of total partitions
 * @returns {Number} partition number to write to
 */
function partition(key, partitionCount)  {
//  var buf = Buffer.isBuffer(key) ? key : Buffer.from(key)
//  return _toPositive(murmur2_32_gc(buf, 0x9747b28c)) % partitionCount
  return _toPositive(murmurhash2_32_gc(key, 0x9747b28c)) % partitionCount
}

/**
 * JS Implementation of MurmurHash2
 * 
 * @author <a href="mailto:gary.court@gmail.com">Gary Court</a>
 * @see http://github.com/garycourt/murmurhash-js
 * @author <a href="mailto:aappleby@gmail.com">Austin Appleby</a>
 * @see http://sites.google.com/site/murmurhash/
 * 
 * @param {string} str ASCII only
 * @param {number} seed Positive integer only
 * @return {number} 32-bit positive integer hash
 */

function murmurhash2_32_gc(str, seed) {
  var
    l = str.length,
    h = seed ^ l,
    i = 0,
    k;
  
  while (l >= 4) {
  	k = 
  	  ((str.charCodeAt(i) & 0xff)) |
  	  ((str.charCodeAt(++i) & 0xff) << 8) |
  	  ((str.charCodeAt(++i) & 0xff) << 16) |
  	  ((str.charCodeAt(++i) & 0xff) << 24);
    
    k = (((k & 0xffff) * 0x5bd1e995) + ((((k >>> 16) * 0x5bd1e995) & 0xffff) << 16));
    k ^= k >>> 24;
    k = (((k & 0xffff) * 0x5bd1e995) + ((((k >>> 16) * 0x5bd1e995) & 0xffff) << 16));

	h = (((h & 0xffff) * 0x5bd1e995) + ((((h >>> 16) * 0x5bd1e995) & 0xffff) << 16)) ^ k;

    l -= 4;
    ++i;
  }
  
  switch (l) {
  case 3: h ^= (str.charCodeAt(i + 2) & 0xff) << 16;
  case 2: h ^= (str.charCodeAt(i + 1) & 0xff) << 8;
  case 1: h ^= (str.charCodeAt(i) & 0xff);
          h = (((h & 0xffff) * 0x5bd1e995) + ((((h >>> 16) * 0x5bd1e995) & 0xffff) << 16));
  }

  h ^= h >>> 13;
  h = (((h & 0xffff) * 0x5bd1e995) + ((((h >>> 16) * 0x5bd1e995) & 0xffff) << 16));
  h ^= h >>> 15;

  return h >>> 0;
}


