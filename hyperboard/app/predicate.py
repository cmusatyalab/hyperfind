"""Any processing for predicates specifically"""

def get_predicate_desc(pred):
    """Processes a predicate to generate a description"""
    # Case by case
    if pred['predicateName'] == "DNN + JIT SVM":
        if ('dataZipState' in pred) and ('folderCount' in pred['dataZipState']):
            res = ""
            for countName, countVal in pred['dataZipState']['folderCount'].items():
                res += " %s: %s," % (countName, countVal)
            return res
    elif pred['predicateName'] == "Face/Body":
        if ('optionMap' in pred and 'minface' in pred['optionMap'] and 'maxface' in pred['optionMap']):
            return "min=%s, max=%s" % (pred['optionMap']['minface'], pred['optionMap']['maxface'])
    elif pred['predicateName'] == "DOG Texture":
        if ('examples' in pred):
            return "num examples: %d" % len(pred['examples'])
    elif pred['predicateName'] == "DNN ImageNet Classify":
        if ('optionMap' in pred and 'targets' in pred['optionMap']):
            return "targets: %s" % pred['optionMap']['targets']
    return "no available description"

