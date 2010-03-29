package com.twitter.elephantbird.pig.store;

import java.io.IOException;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.pig.data.Tuple;

import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message.Builder;
import com.twitter.elephantbird.util.Protobufs;
import com.twitter.elephantbird.util.TypeRef;


/**
 * Serializes Pig Tuples into Base-64 encoded, line-delimited protocol buffers.
 * The fields in the pig tuple must correspond exactly to the fields in the protobuf, as
 * no name-matching is performed (names of the tuple fields are not currently accessible to
 * a StoreFunc. It will be in 0.7, so something more flexible will be possible)
 *
 * @param <M> Protocol Buffer Message class being serialized
 */
public abstract class LzoProtobufB64LinePigStorage<M extends Message> extends LzoBaseStoreFunc {

  private TypeRef<M> typeRef_;
  private Base64 base64_ = new Base64();
  List<FieldDescriptor> fieldDescriptors_;

  protected void setTypeRef(TypeRef<M> typeRef) {
    typeRef_ = typeRef;
    fieldDescriptors_ = Protobufs.getMessageDescriptor(typeRef_.getRawClass()).getFields();
  }

  public void putNext(Tuple f) throws IOException {
    if (f == null) return;
    Builder builder = Protobufs.getMessageBuilder(typeRef_.getRawClass());
    for (int i = 0; i < fieldDescriptors_.size() && i < f.size(); i++) {
      if (f.get(i) != null) {
        builder.setField(fieldDescriptors_.get(i), f.get(i));
      }
    }
    os_.write(base64_.encode(builder.build().toByteArray()));
    os_.write("\n".getBytes("UTF-8"));
  }

}
