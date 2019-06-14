from onnx_initializers_extractor import extract_initializers

mnist = "/vol/home/minakovas/ONNX/mnist/mnist.onnx"

metadataAlexnet = "/vol/home/minakovas/espam2/output_models/bvlc_alexnet/inits_metadata.json"
alexnet = "/vol/home/minakovas/ONNX/bvlc_alexnet/alexnet.onnx"
alexnet_weights_dir = "/vol/home/minakovas/espam2/output_models/weights_tst/alexnet_zip_npz"

metadataDollyo1 = "/vol/home/minakovas/espam2/output_models/GAtest1552300716787/inits_metadata.json"
dollyso1 = "/vol/home/minakovas/espam2/Dollys/o1.onnx"
dollyso1_weights_dir = "/vol/home/minakovas/espam2/output_models/weights_tst/Dollyso1_npz"

metadataDollyo92 = "/vol/home/minakovas/espam2/output_models/GAtest1552300723338/inits_metadata.json"
dollyso92 = "/vol/home/minakovas/espam2/Dollys/o92.onnx"
dollyso92_weights_dir = "/vol/home/minakovas/espam2/output_models/weights_tst/Dollys092_npz"

metadataInception = "/vol/home/minakovas/espam2/output_models/inception_v1/inits_metadata.json"
inception = "/vol/home/minakovas/ONNX/inception_v1/model.onnx"
inception_weights_dir = "/vol/home/minakovas/espam2/output_models/weights_tst/Inception_zip_npz"

mnist = "/vol/home/minakovas/ONNX/mnist/mnist.onnx"
mnist_metadata = "/vol/home/minakovas/espam2/output_models/CNTKGraph/inits_metadata.json"
mnist_weights_dir = "/vol/home/minakovas/espam2/output_models/CNTKGraph/weights_zip_npz"

#extract_initializers(alexnet, metadataAlexnet, alexnet_weights_dir, True)
#extract_initializers(inception,metadataInception,inception_weights_dir,True)
extract_initializers(mnist, mnist_metadata,"./")





