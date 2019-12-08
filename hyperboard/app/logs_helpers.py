"""Helper functions for processing logs, mostly file/path search functions"""

import os

def float_div(num, denom):
    """Safe division of two floats, and returns 0 if denom is 0"""
    if int(denom) == 0:
        return 0.0
    return float(num) / float(denom)

def sort_numbered_files(folder, ext=""):
    """Return sorted list of subfiles in the form [x]ext
    where x is an integer, and files have given extension
    """
    assert os.path.isdir(folder)

    def is_in_format(filename):
        """Returns true if filename is in form [filename]ext"""
        if not filename.endswith(ext):
            return False
        return filename[: len(filename) - len(ext)].isdigit()

    in_format = filter(lambda x: is_in_format(x), os.listdir(folder))

    def separate_int(filename):
        """Returns the integer format"""
        return int(filename[: len(filename) - len(ext)])

    numbered_files = map(lambda x: separate_int(x), in_format)
    return sorted(numbered_files)


def get_valid_search_paths(root_folder, depth=2):
    """Return a list of "valid" search paths starting from given root_folder
    and within a specified depth. Since it is expensive to try to process all
    files, we do a crude estimate. Namely, we mark a folder as "valid" if it
    contains subfolders with consecutive numbers (i.e. "0/", "1/", "2/", etc.)
    """
    candidate_searches = []
    def get_valid_search_paths_(search_from_path, depth):
        nonlocal candidate_searches
        """Performs search from given child path"""
        assert depth >= 0
        assert os.path.isdir(search_from_path)

        # it is a "candidate" search if it has direct subfolders of 0/, 1/, 2/, ...
        sorted_subfolders = sort_numbered_files(search_from_path, ext="")
        if len(sorted_subfolders) > 0:
            good = True
            for i in range(len(sorted_subfolders)):
                if sorted_subfolders[i] != i:
                    good = False
                    print(i, "not in", sorted_subfolders)
                    break
            if good:
                print(sorted_subfolders, "good", search_from_path)
                candidate_searches.append(search_from_path)
                print(candidate_searches)

        if depth > 0:
            for child in os.listdir(search_from_path):
                new_path = os.path.join(search_from_path, child)
                if os.path.isdir(new_path):
                    get_valid_search_paths_(new_path, depth - 1)
        return

    get_valid_search_paths_(root_folder, depth)
    return candidate_searches

