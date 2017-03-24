// Copyright 2017 Foursquare Labs Inc. All Rights Reserved.

package io.fsq.rogue.query.testlib

import com.mongodb.{ReadPreference, WriteConcern}
import io.fsq.rogue.Query
import io.fsq.rogue.adapter.MongoCollectionFactory
import io.fsq.rogue.connection.MongoClientManager
import io.fsq.rogue.index.{IndexedRecord, UntypedMongoIndex}
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry


class TrivialORMMongoCollectionFactory[MongoClient, MongoDatabase, MongoCollection[_]](
  clientManager: MongoClientManager[MongoClient, MongoDatabase, MongoCollection]
) extends MongoCollectionFactory[
  MongoCollection,
  Document,
  TrivialORMMetaRecord[_],
  TrivialORMRecord
] {

  override def documentClass: Class[Document] = classOf[Document]

  override def documentToString(document: Document): String = document.toJson

  override def getCodecRegistryFromQuery[M <: TrivialORMMetaRecord[_]](
    query: Query[M, _, _]
  ): CodecRegistry = {
    clientManager.getCodecRegistryOrThrow(query.meta.mongoIdentifier)
  }

  override def getMongoCollectionFromQuery[M <: TrivialORMMetaRecord[_]](
    query: Query[M, _, _],
    readPreferenceOpt: Option[ReadPreference] = None,
    writeConcernOpt: Option[WriteConcern] = None
  ): MongoCollection[Document] = {
    clientManager.useCollection(
      query.meta.mongoIdentifier,
      query.collectionName,
      documentClass,
      readPreferenceOpt,
      writeConcernOpt
    )(
      identity
    )
  }

  override def getMongoCollectionFromRecord[R <: TrivialORMRecord](
    record: R,
    readPreferenceOpt: Option[ReadPreference] = None,
    writeConcernOpt: Option[WriteConcern] = None
  ): MongoCollection[Document] = {
    clientManager.useCollection(
      record.meta.mongoIdentifier,
      record.meta.collectionName,
      documentClass,
      readPreferenceOpt,
      writeConcernOpt
    )(
      identity
    )
  }

  override def getInstanceNameFromQuery[M <: TrivialORMMetaRecord[_]](
    query: Query[M, _, _]
  ): String = {
    query.meta.mongoIdentifier.toString
  }

  override def getInstanceNameFromRecord[R <: TrivialORMRecord](record: R): String = {
    record.meta.mongoIdentifier.toString
  }

  override def getIndexes[M <: TrivialORMMetaRecord[_]](
    query: Query[M, _, _]
  ): Option[Seq[UntypedMongoIndex]] = {
    query.meta match {
      case indexed: IndexedRecord[_] => Some(indexed.mongoIndexList)
      case _ => None
    }
  }
}
